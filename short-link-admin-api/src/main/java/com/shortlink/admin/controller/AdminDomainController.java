package com.shortlink.admin.controller;

import com.shortlink.admin.dto.DomainRequest;
import com.shortlink.admin.dto.DomainVO;
import com.shortlink.admin.service.AdminDomainService;
import com.shortlink.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "域名管理", description = "平台短链域名的增删与启停")
@RestController
@RequestMapping("/admin/domain")
@RequiredArgsConstructor
public class AdminDomainController {

    private final AdminDomainService adminDomainService;

    @Operation(summary = "域名列表", description = "返回全部域名、启用状态与该域名下已生成的短链数量")
    @GetMapping
    public Result<List<DomainVO>> list() {
        return Result.ok(adminDomainService.list());
    }

    @Operation(summary = "新增域名", description = "域名不可重复；标记为默认域名时会自动取消原有的默认域名")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody DomainRequest request) {
        adminDomainService.create(request);
        return Result.ok();
    }

    @Operation(summary = "启用或停用域名", description = "默认域名不能停用，需先切换默认域名")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(name = "id", description = "域名 ID", required = true) @PathVariable long id,
            @Parameter(name = "status", description = "1 启用，0 停用", required = true) @RequestParam int status) {
        adminDomainService.updateStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "删除域名", description = "默认域名与已生成短链的域名不能删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(name = "id", description = "域名 ID", required = true) @PathVariable long id) {
        adminDomainService.delete(id);
        return Result.ok();
    }
}
