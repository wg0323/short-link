package com.shortlink.user.dto;

public class DomainVO {

    private final Long id;

    private final String domain;

    /** 是否为平台默认域名。 */
    private final boolean defaultFlag;

    public DomainVO(Long id, String domain, boolean defaultFlag) {
        this.id = id;
        this.domain = domain;
        this.defaultFlag = defaultFlag;
    }

    public Long getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }

    public boolean isDefaultFlag() {
        return defaultFlag;
    }
}
