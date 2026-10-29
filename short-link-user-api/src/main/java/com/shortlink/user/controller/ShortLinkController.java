package com.shortlink.user.controller;

import com.shortlink.common.api.PageVO;
import com.shortlink.common.api.Result;
import com.shortlink.common.ratelimit.RateLimit;
import com.shortlink.common.ratelimit.RateLimitType;
import com.shortlink.common.security.UserContext;
import com.shortlink.user.dto.CreateLinkRequest;
import com.shortlink.user.dto.CreateLinkResult;
import com.shortlink.user.dto.LinkQuery;
import com.shortlink.user.dto.LinkVO;
import com.shortlink.user.dto.UpdateLinkRequest;
import com.shortlink.user.service.ShortLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "短链管理", description = "生成、查询、修改与删除短链")
@RestController
@RequestMapping("/api/link")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @Operation(summary = "生成短链", description = "同一用户在同一域名下重复提交同一条长链接，返回已存在的短链并标记 duplicated")
    @PostMapping("/generate")
    @RateLimit(type = RateLimitType.USER, limit = 60, windowSeconds = 60, name = "link_generate")
    public Result<CreateLinkResult> generate(@Valid @RequestBody CreateLinkRequest request) {
        return Result.ok(shortLinkService.generate(UserContext.require(), request));
    }

    @Operation(summary = "分页查询短链", description = "按备注名、域名、状态与创建时间区间筛选，默认不包含已删除")
    @GetMapping("/page")
    public Result<PageVO<LinkVO>> page(@ParameterObject @ModelAttribute LinkQuery query) {
        return Result.ok(shortLinkService.page(UserContext.require(), query));
    }

    @Operation(summary = "查询短链详情", description = "按短码返回单条短链，短码不存在或不属于当前用户时返回 404")
    @GetMapping("/{code}")
    public Result<LinkVO> detail(
            @Parameter(name = "code", description = "短码", required = true) @PathVariable String code) {
        return Result.ok(shortLinkService.detail(UserContext.require(), code));
    }

    @Operation(summary = "修改短链", description = "备注、访问密码、生效时间与状态可单独修改，传 null 表示不变")
    @PutMapping("/{code}")
    public Result<Void> update(
            @Parameter(name = "code", description = "短码", required = true) @PathVariable String code,
            @Valid @RequestBody UpdateLinkRequest request) {
        shortLinkService.update(UserContext.require(), code, request);
        return Result.ok();
    }

    @Operation(summary = "删除短链", description = "逻辑删除，并清掉跳转端的缓存")
    @DeleteMapping("/{code}")
    public Result<Void> delete(
            @Parameter(name = "code", description = "短码", required = true) @PathVariable String code) {
        shortLinkService.delete(UserContext.require(), code);
        return Result.ok();
    }

    @Operation(summary = "批量删除短链", description = "传入短链 ID 列表，返回实际删除条数")
    @DeleteMapping("/batch")
    public Result<Integer> deleteBatch(@RequestBody List<Long> ids) {
        return Result.ok(shortLinkService.deleteBatch(UserContext.require(), ids));
    }
}
