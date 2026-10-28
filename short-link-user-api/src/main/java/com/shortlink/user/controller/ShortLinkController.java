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
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    public ShortLinkController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @Operation(summary = "生成短链（同一长链接重复提交返回同一条）")
    @PostMapping("/generate")
    @RateLimit(type = RateLimitType.USER, limit = 60, windowSeconds = 60, name = "link_generate")
    public Result<CreateLinkResult> generate(@Valid @RequestBody CreateLinkRequest request) {
        return Result.ok(shortLinkService.generate(UserContext.require(), request));
    }

    @Operation(summary = "分页查询短链")
    @GetMapping("/page")
    public Result<PageVO<LinkVO>> page(@ModelAttribute LinkQuery query) {
        return Result.ok(shortLinkService.page(UserContext.require(), query));
    }

    @Operation(summary = "查询短链详情")
    @GetMapping("/{code}")
    public Result<LinkVO> detail(@PathVariable String code) {
        return Result.ok(shortLinkService.detail(UserContext.require(), code));
    }

    @Operation(summary = "修改短链")
    @PutMapping("/{code}")
    public Result<Void> update(@PathVariable String code, @Valid @RequestBody UpdateLinkRequest request) {
        shortLinkService.update(UserContext.require(), code, request);
        return Result.ok();
    }

    @Operation(summary = "删除短链")
    @DeleteMapping("/{code}")
    public Result<Void> delete(@PathVariable String code) {
        shortLinkService.delete(UserContext.require(), code);
        return Result.ok();
    }

    @Operation(summary = "批量删除短链")
    @DeleteMapping("/batch")
    public Result<Integer> deleteBatch(@RequestBody List<Long> ids) {
        return Result.ok(shortLinkService.deleteBatch(UserContext.require(), ids));
    }
}
