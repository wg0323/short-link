package com.shortlink.user.controller;

import com.shortlink.common.api.Result;
import com.shortlink.user.dto.DomainVO;
import com.shortlink.user.service.DomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "短链域名", description = "生成短链时可选用的域名")
@RestController
@RequestMapping("/api/domain")
@RequiredArgsConstructor
public class DomainController {

    private final DomainService domainService;

    @Operation(summary = "可用域名列表", description = "只返回启用中的域名，默认域名排在首位")
    @GetMapping("/list")
    public Result<List<DomainVO>> list() {
        return Result.ok(domainService.listEnabled());
    }
}
