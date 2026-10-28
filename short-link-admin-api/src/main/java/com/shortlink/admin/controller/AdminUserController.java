package com.shortlink.admin.controller;

import com.shortlink.admin.dto.AdminUserVO;
import com.shortlink.admin.dto.ResetPasswordRequest;
import com.shortlink.admin.service.AdminUserService;
import com.shortlink.common.api.PageVO;
import com.shortlink.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(summary = "分页查询注册用户")
    @GetMapping("/page")
    public Result<PageVO<AdminUserVO>> page(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(adminUserService.page(keyword, status, page, size));
    }

    @Operation(summary = "启用或禁用用户（禁用后立即生效）")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable long id, @RequestParam int status) {
        adminUserService.updateStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable long id,
                                      @Valid @RequestBody ResetPasswordRequest request) {
        adminUserService.resetPassword(id, request.getPassword());
        return Result.ok();
    }

    @Operation(summary = "重置用户配额（清零用量并解除短链暂停）")
    @PutMapping("/{id}/quota")
    public Result<Void> resetQuota(@PathVariable long id) {
        adminUserService.resetQuota(id);
        return Result.ok();
    }
}
