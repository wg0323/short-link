package com.shortlink.user.controller;

import com.shortlink.common.api.Result;
import com.shortlink.common.security.UserContext;
import com.shortlink.user.dto.ChangePasswordRequest;
import com.shortlink.user.dto.QuotaVO;
import com.shortlink.user.dto.UpdateProfileRequest;
import com.shortlink.user.dto.UserProfileVO;
import com.shortlink.user.service.QuotaService;
import com.shortlink.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Tag(name = "个人中心", description = "个人信息查询与修改")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserProfileService userProfileService;

    private final QuotaService quotaService;

    public UserController(UserProfileService userProfileService, QuotaService quotaService) {
        this.userProfileService = userProfileService;
        this.quotaService = quotaService;
    }

    @Operation(summary = "查询个人信息")
    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        return Result.ok(userProfileService.profile(UserContext.require()));
    }

    @Operation(summary = "修改昵称")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        userProfileService.updateNickname(UserContext.require(), request.getNickname());
        return Result.ok();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(UserContext.require(),
                request.getOldPassword(), request.getNewPassword());
        return Result.ok();
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<UserProfileVO> uploadAvatar(@RequestPart("file") MultipartFile file) {
        return Result.ok(userProfileService.updateAvatar(UserContext.require(), file));
    }

    @Operation(summary = "查询套餐与用量")
    @GetMapping("/quota")
    public Result<QuotaVO> quota() {
        return Result.ok(quotaService.overview(UserContext.require()));
    }
}
