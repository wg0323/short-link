package com.shortlink.user.controller;

import com.shortlink.common.api.Result;
import com.shortlink.user.dto.DomainVO;
import com.shortlink.user.service.DomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "短链域名", description = "生成短链时可选用的域名")
@RestController
@RequestMapping("/api/domain")
public class DomainController {

    private final DomainService domainService;

    public DomainController(DomainService domainService) {
        this.domainService = domainService;
    }

    @Operation(summary = "可用域名列表")
    @GetMapping("/list")
    public Result<List<DomainVO>> list() {
        return Result.ok(domainService.listEnabled());
    }
}
