package com.shortlink.admin.controller;

import com.shortlink.admin.dto.DomainRequest;
import com.shortlink.admin.dto.DomainVO;
import com.shortlink.admin.service.AdminDomainService;
import com.shortlink.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class AdminDomainController {

    private final AdminDomainService adminDomainService;

    public AdminDomainController(AdminDomainService adminDomainService) {
        this.adminDomainService = adminDomainService;
    }

    @Operation(summary = "域名列表")
    @GetMapping
    public Result<List<DomainVO>> list() {
        return Result.ok(adminDomainService.list());
    }

    @Operation(summary = "新增域名")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody DomainRequest request) {
        adminDomainService.create(request);
        return Result.ok();
    }

    @Operation(summary = "启用或停用域名")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable long id, @RequestParam int status) {
        adminDomainService.updateStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "删除域名（默认域名与已使用的域名不可删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable long id) {
        adminDomainService.delete(id);
        return Result.ok();
    }
}
