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
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class StatController {

    private final StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    @Operation(summary = "概览：区间汇总与链接总数")
    @GetMapping("/overview")
    public Result<StatOverview> overview(@RequestParam(required = false) String code,
                                         @RequestParam(defaultValue = "today") String range,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return Result.ok(statService.overview(UserContext.require(), code, range, start, end));
    }

    @Operation(summary = "访问趋势（按天）")
    @GetMapping("/trend")
    public Result<List<StatTrendPoint>> trend(@RequestParam(required = false) String code,
                                              @RequestParam(defaultValue = "last30") String range,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return Result.ok(statService.trend(UserContext.require(), code, range, start, end));
    }

    @Operation(summary = "地域分布 Top10")
    @GetMapping("/region")
    public Result<List<StatRegionItem>> region(@RequestParam(required = false) String code,
                                               @RequestParam(defaultValue = "last30") String range,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return Result.ok(statService.region(UserContext.require(), code, range, start, end));
    }

    @Operation(summary = "访问明细分页（IP 已脱敏）")
    @GetMapping("/click/page")
    public Result<PageVO<ClickLogVO>> clickPage(@RequestParam(required = false) String code,
                                                @RequestParam(defaultValue = "today") String range,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return Result.ok(statService.clickPage(UserContext.require(), code, range, start, end, page, size));
    }
}
