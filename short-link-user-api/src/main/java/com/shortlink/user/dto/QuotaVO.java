package com.shortlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 套餐与用量视图。
 *
 * <p>三个上限字段为 null 表示「不限」；用量取 Redis 实时值与库内快照的较大者。</p>
 */
@Schema(description = "套餐与用量")
public class QuotaVO {

    @Schema(description = "套餐编码", example = "free")
    private String packageCode;

    @Schema(description = "套餐名称", example = "免费版")
    private String packageName;

    @Schema(description = "累计可生成短链上限，null 表示不限")
    private Long totalGenerate;

    @Schema(description = "每日可生成短链上限，null 表示不限", example = "100")
    private Long dailyGenerate;

    @Schema(description = "累计访问量上限，null 表示不限")
    private Long totalClick;

    @Schema(description = "累计已生成条数", example = "12")
    private Long usedGenerate;

    @Schema(description = "累计已产生访问量", example = "340")
    private Long usedClick;

    /** 今日已新建条数。 */
    @Schema(description = "今日已新建条数", example = "3")
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
