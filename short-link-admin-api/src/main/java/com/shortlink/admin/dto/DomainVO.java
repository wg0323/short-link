package com.shortlink.admin.dto;

import java.time.LocalDateTime;

public class DomainVO {

    private final Long id;

    private final String domain;

    private final boolean defaultFlag;

    private final Integer status;

    private final long linkCount;

    private final LocalDateTime createdAt;

    public DomainVO(Long id, String domain, boolean defaultFlag, Integer status,
                    long linkCount, LocalDateTime createdAt) {
        this.id = id;
        this.domain = domain;
        this.defaultFlag = defaultFlag;
        this.status = status;
        this.linkCount = linkCount;
        this.createdAt = createdAt;
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

    public Integer getStatus() {
        return status;
    }

    public long getLinkCount() {
        return linkCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
