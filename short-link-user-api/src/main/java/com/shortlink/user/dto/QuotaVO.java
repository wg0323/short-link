package com.shortlink.user.dto;

/**
 * 套餐与用量视图。
 *
 * <p>三个上限字段为 null 表示「不限」；用量取 Redis 实时值与库内快照的较大者。</p>
 */
public class QuotaVO {

    private String packageCode;

    private String packageName;

    private Long totalGenerate;

    private Long dailyGenerate;

    private Long totalClick;

    private Long usedGenerate;

    private Long usedClick;

    /** 今日已新建条数。 */
    private Long dailyGenerated;

    public String getPackageCode() {
        return packageCode;
    }

    public void setPackageCode(String packageCode) {
        this.packageCode = packageCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public Long getDailyGenerated() {
        return dailyGenerated;
    }

    public void setDailyGenerated(Long dailyGenerated) {
        this.dailyGenerated = dailyGenerated;
    }
}
