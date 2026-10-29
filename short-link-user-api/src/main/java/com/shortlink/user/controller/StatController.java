package com.shortlink.user.controller;

import com.shortlink.common.api.PageVO;
import com.shortlink.common.api.Result;
import com.shortlink.common.security.UserContext;
import com.shortlink.user.dto.ClickLogVO;
import com.shortlink.user.dto.StatOverview;
import com.shortlink.user.dto.StatRegionItem;
import com.shortlink.user.dto.StatTrendPoint;
import com.shortlink.user.service.StatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "数据统计", description = "PV/UV/IP 趋势、地域分布与访问明细")
@RestController
@RequestMapping("/api/stat")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @Operation(summary = "概览数据", description = "区间内的 PV / UV / IP 数与链接总数，今天的数据取 Redis 实时值")
    @GetMapping("/overview")
    public Result<StatOverview> overview(
            @Parameter(name = "code", description = "短码，不传则汇总该用户全部短链")
            @RequestParam(required = false) String code,
            @Parameter(name = "range", description = "时间范围：today / last7 / last30 / custom")
            @RequestParam(defaultValue = "today") String range,
            @Parameter(name = "start", description = "开始日期 yyyy-MM-dd，range=custom 时生效")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(name = "end", description = "结束日期 yyyy-MM-dd，range=custom 时生效")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return Result.ok(statService.overview(UserContext.require(), code, range, start, end));
    }

    @Operation(summary = "访问趋势", description = "按天返回 PV / UV / IP 数，默认最近 30 天")
    @GetMapping("/trend")
    public Result<List<StatTrendPoint>> trend(
            @Parameter(name = "code", description = "短码，不传则汇总该用户全部短链")
            @RequestParam(required = false) String code,
            @Parameter(name = "range", description = "时间范围：today / last7 / last30 / custom")
            @RequestParam(defaultValue = "last30") String range,
            @Parameter(name = "start", description = "开始日期 yyyy-MM-dd，range=custom 时生效")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(name = "end", description = "结束日期 yyyy-MM-dd，range=custom 时生效")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return Result.ok(statService.trend(UserContext.require(), code, range, start, end));
    }

    @Operation(summary = "地域分布 Top10", description = "按省份汇总访问量，地域由离线 IP 库解析")
    @GetMapping("/region")
    public Result<List<StatRegionItem>> region(
            @Parameter(name = "code", description = "短码，不传则汇总该用户全部短链")
            @RequestParam(required = false) String code,
            @Parameter(name = "range", description = "时间范围：today / last7 / last30 / custom")
            @RequestParam(defaultValue = "last30") String range,
            @Parameter(name = "start", description = "开始日期 yyyy-MM-dd，range=custom 时生效")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(name = "end", description = "结束日期 yyyy-MM-dd，range=custom 时生效")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return Result.ok(statService.region(UserContext.require(), code, range, start, end));
    }

    @Operation(summary = "访问明细分页", description = "返回逐条访问记录，IP 已脱敏")
    @GetMapping("/click/page")
    public Result<PageVO<ClickLogVO>> clickPage(
            @Parameter(name = "code", description = "短码，不传则汇总该用户全部短链")
            @RequestParam(required = false) String code,
            @Parameter(name = "range", description = "时间范围：today / last7 / last30 / custom")
            @RequestParam(defaultValue = "today") String range,
            @Parameter(name = "start", description = "开始日期 yyyy-MM-dd，range=custom 时生效")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(name = "end", description = "结束日期 yyyy-MM-dd，range=custom 时生效")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @Parameter(name = "page", description = "页码，从 1 开始")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(name = "size", description = "每页条数，最大 100")
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(statService.clickPage(UserContext.require(), code, range, start, end, page, size));
    }
}
