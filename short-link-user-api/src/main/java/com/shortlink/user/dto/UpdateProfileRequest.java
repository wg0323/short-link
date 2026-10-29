package com.shortlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Size;

@Schema(description = "修改个人信息请求")
public class UpdateProfileRequest {

    @Schema(description = "昵称，最长 20 个字符", example = "小明")
    @Size(max = 20, message = "昵称最长 20 个字符")
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
