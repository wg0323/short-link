package com.shortlink.redirect.support;

import org.lionsoul.ip2region.xdb.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

/**
 * 离线 IP 归属地解析（ip2region）。
 *
 * <p>需要把 {@code ip2region.xdb} 放到 classpath 或者磁盘上再配置
 * {@code shortlink.redirect.ip2region-path}；找不到数据文件时降级为「未知」，
 * 不影响跳转主链路。</p>
 */
@Service
public class IpRegionService {

    private static final Logger log = LoggerFactory.getLogger(IpRegionService.class);

    private final Searcher searcher;

    public IpRegionService(ResourceLoader resourceLoader,
                           @Value("${shortlink.redirect.ip2region-path:classpath:ip2region.xdb}") String location) {
        this.searcher = load(resourceLoader, location);
    }

    /**
     * 返回 [省份, 城市]，解析失败时返回「未知」。
     */
    public String[] resolve(String ip) {
        if (searcher == null || ip == null || ip.isEmpty()) {
            return new String[]{"未知", ""};
        }
        try {
            String region = searcher.search(ip);
            String[] parts = region.split("\\|");
            if (parts.length < 4 || "0".equals(parts[2])) {
                return new String[]{"未知", ""};
            }
            return new String[]{parts[2], parts[3]};
        } catch (Exception e) {
            return new String[]{"未知", ""};
        }
    }

    private Searcher load(ResourceLoader resourceLoader, String location) {
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                log.warn("未找到 ip2region 数据文件（{}），地域统计将显示为未知", location);
                return null;
            }
            byte[] content = StreamUtils.copyToByteArray(resource.getInputStream());
            return Searcher.newWithBuffer(content);
        } catch (Exception e) {
            log.warn("ip2region 数据文件加载失败（{}），地域统计将显示为未知", location, e);
            return null;
        }
    }
}
