package com.shortlink.common.sentinel;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Sentinel 接入：连接 Dashboard 并从 Nacos 加载规则。
 *
 * <p>规则不写死在代码里，全部放 Nacos，改完秒级生效。三个 dataId 分别对应
 * 流控、熔断、热点参数，内容为对应的规则 JSON 数组。Nacos 地址留空则只用内存规则，
 * Dashboard 地址留空则不上报。</p>
 */
@Configuration
public class SentinelRuleConfig {

    private static final Logger log = LoggerFactory.getLogger(SentinelRuleConfig.class);

    private static final String FLOW_DATA_ID = "short-link.sentinel.flow.json";

    private static final String DEGRADE_DATA_ID = "short-link.sentinel.degrade.json";

    private static final String PARAM_FLOW_DATA_ID = "short-link.sentinel.param-flow.json";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public SentinelRuleConfig(Environment environment,
                              @Value("${shortlink.sentinel.dashboard:}") String dashboard,
                              @Value("${shortlink.sentinel.nacos-server:}") String nacosServer,
                              @Value("${shortlink.sentinel.nacos-group:SENTINEL_GROUP}") String group) {
        initDashboard(environment, dashboard);
        initRules(nacosServer, group);
    }

    private void initDashboard(Environment environment, String dashboard) {
        if (!StringUtils.hasText(dashboard)) {
            return;
        }
        // Sentinel 只认系统属性，必须在第一次使用 Sentinel 之前设置
        System.setProperty("csp.sentinel.dashboard.server", dashboard);
        System.setProperty("project.name", environment.getProperty("spring.application.name", "unknown"));
        log.info("Sentinel Dashboard 已配置 address={}", dashboard);
    }

    private void initRules(String nacosServer, String group) {
        if (!StringUtils.hasText(nacosServer)) {
            log.info("未配置 Nacos 地址，Sentinel 规则仅使用内存规则");
            return;
        }
        try {
            ReadableDataSource<String, List<FlowRule>> flow = new NacosDataSource<>(nacosServer, group,
                    FLOW_DATA_ID, source -> parse(source, new TypeReference<List<FlowRule>>() {
                    }));
            FlowRuleManager.register2Property(flow.getProperty());

            ReadableDataSource<String, List<DegradeRule>> degrade = new NacosDataSource<>(nacosServer, group,
                    DEGRADE_DATA_ID, source -> parse(source, new TypeReference<List<DegradeRule>>() {
                    }));
            DegradeRuleManager.register2Property(degrade.getProperty());

            ReadableDataSource<String, List<ParamFlowRule>> paramFlow = new NacosDataSource<>(nacosServer, group,
                    PARAM_FLOW_DATA_ID, source -> parse(source, new TypeReference<List<ParamFlowRule>>() {
                    }));
            ParamFlowRuleManager.register2Property(paramFlow.getProperty());

            log.info("Sentinel 规则已挂到 Nacos server={} group={} flow={} degrade={} paramFlow={}",
                    nacosServer, group, FlowRuleManager.getRules().size(),
                    DegradeRuleManager.getRules().size(), ParamFlowRuleManager.getRules().size());
        } catch (Exception e) {
            // 拉不到规则时退回内存规则，不能因为配置中心不可用导致服务起不来
            log.error("Sentinel 规则加载失败 server={} group={}", nacosServer, group, e);
        }
    }

    private <T> List<T> parse(String source, TypeReference<List<T>> type) {
        if (!StringUtils.hasText(source)) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(source, type);
        } catch (Exception e) {
            // 解析失败退化成空规则，不能让一段坏配置影响服务启动
            log.warn("Sentinel 规则解析失败，已忽略该配置", e);
            return Collections.emptyList();
        }
    }
}
