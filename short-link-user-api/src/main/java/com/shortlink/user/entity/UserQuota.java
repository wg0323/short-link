package com.shortlink.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户配额：权威值落库，实时用量在 Redis。
 */
@TableName("user_quota")
public class UserQuota {

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    private String packageCode;

    private Long totalGenerate;

    private Long dailyGenerate;

    private Long totalClick;

    private Long usedGenerate;

    private Long usedClick;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    /** 1 正常 2 已超限 3 已过期。 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPackageCode() {
        return packageCode;
    }

    public void setPackageCode(String packageCode) {
        this.packageCode = packageCode;
    }

    public Long getTotalGenerate() {
        return totalGenerate;
    }

    public void setTotalGenerate(Long totalGenerate) {
        this.totalGenerate = totalGenerate;
    }

    public Long getDailyGenerate() {
        return dailyGenerate;
    }

    public void setDailyGenerate(Long dailyGenerate) {
        this.dailyGenerate = dailyGenerate;
    }

    public Long getTotalClick() {
        return totalClick;
    }

    public void setTotalClick(Long totalClick) {
        this.totalClick = totalClick;
    }

    public Long getUsedGenerate() {
        return usedGenerate;
    }

    public void setUsedGenerate(Long usedGenerate) {
        this.usedGenerate = usedGenerate;
    }

    public Long getUsedClick() {
        return usedClick;
    }

    public void setUsedClick(Long usedClick) {
        this.usedClick = usedClick;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
