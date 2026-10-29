package com.shortlink.admin.controller;

import com.shortlink.admin.dto.AdminUserVO;
import com.shortlink.admin.dto.ResetPasswordRequest;
import com.shortlink.admin.service.AdminUserService;
import com.shortlink.common.api.PageVO;
import com.shortlink.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "用户管理", description = "注册用户查询、禁用与重置密码")
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "分页查询注册用户", description = "按手机号或昵称模糊搜索，可按启用状态筛选")
    @GetMapping("/page")
    public Result<PageVO<AdminUserVO>> page(
            @Parameter(name = "keyword", description = "手机号或昵称，不传表示不筛选")
            @RequestParam(required = false) String keyword,
            @Parameter(name = "status", description = "1 正常，0 禁用，不传表示全部")
            @RequestParam(required = false) Integer status,
            @Parameter(name = "page", description = "页码，从 1 开始")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(name = "size", description = "每页条数，最大 100")
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(adminUserService.page(keyword, status, page, size));
    }

    @Operation(summary = "启用或禁用用户", description = "禁用会把标记写进 Redis，用户端下次请求立即生效")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(name = "id", description = "用户 ID", required = true) @PathVariable long id,
            @Parameter(name = "status", description = "1 启用，0 禁用", required = true) @RequestParam int status) {
        adminUserService.updateStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "重置用户密码", description = "重置后删除该用户的刷新令牌，必须用新密码重新登录")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(
            @Parameter(name = "id", description = "用户 ID", required = true) @PathVariable long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        adminUserService.resetPassword(id, request.getPassword());
        return Result.ok();
    }

    @Operation(summary = "重置用户配额", description = "清零已用量并解除因超额而暂停的短链")
    @PutMapping("/{id}/quota")
    public Result<Void> resetQuota(
            @Parameter(name = "id", description = "用户 ID", required = true) @PathVariable long id) {
        adminUserService.resetQuota(id);
        return Result.ok();
    }
}
