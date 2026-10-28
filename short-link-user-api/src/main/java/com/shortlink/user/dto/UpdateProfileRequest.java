package com.shortlink.user.dto;

import javax.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 20, message = "昵称最长 20 个字符")
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
