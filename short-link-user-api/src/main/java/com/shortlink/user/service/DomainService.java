package com.shortlink.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortlink.common.api.ResultCode;
import com.shortlink.common.exception.BizException;
import com.shortlink.user.dto.DomainVO;
import com.shortlink.user.entity.Domain;
import com.shortlink.user.mapper.DomainMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 短链域名：平台维护多个域名，用户生成时选择，不涉及自助绑定与证书签发。
 */
@Service
public class DomainService {

    private final DomainMapper domainMapper;

    private final String scheme;

    public DomainService(DomainMapper domainMapper,
                         @Value("${shortlink.link.scheme:http}") String scheme) {
        this.domainMapper = domainMapper;
        this.scheme = scheme;
    }

    public List<DomainVO> listEnabled() {
        List<Domain> domains = domainMapper.selectList(new LambdaQueryWrapper<Domain>()
                .eq(Domain::getStatus, 1)
                .orderByDesc(Domain::getIsDefault)
                .orderByAsc(Domain::getId));
        return domains.stream()
                .map(d -> new DomainVO(d.getId(), d.getDomain(), Integer.valueOf(1).equals(d.getIsDefault())))
                .collect(Collectors.toList());
    }

    /**
     * 取指定域名，未指定则取默认域名；不可用时抛业务异常。
     */
    public Domain getOrDefault(Long domainId) {
        if (domainId != null) {
            Domain domain = domainMapper.selectById(domainId);
            if (domain == null || !Integer.valueOf(1).equals(domain.getStatus())) {
                throw new BizException(ResultCode.PARAM_ERROR, "短链域名不可用");
            }
            return domain;
        }
        Domain domain = domainMapper.selectOne(new LambdaQueryWrapper<Domain>()
                .eq(Domain::getIsDefault, 1)
                .eq(Domain::getStatus, 1)
                .last("limit 1"));
        if (domain == null) {
            throw new BizException(ResultCode.SYSTEM_ERROR, "未配置默认短链域名");
        }
        return domain;
    }

    public Map<Long, String> nameMap() {
        return domainMapper.selectList(null).stream()
                .collect(Collectors.toMap(Domain::getId, Domain::getDomain, (a, b) -> a));
    }

    public String buildShortUrl(Domain domain, String code) {
        return scheme + "://" + domain.getDomain() + "/" + code;
    }

    public String buildShortUrl(String domainName, String code) {
        return scheme + "://" + domainName + "/" + code;
    }
}
