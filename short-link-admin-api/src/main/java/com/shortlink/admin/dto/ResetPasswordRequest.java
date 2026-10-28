package com.shortlink.admin.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需为 8-32 位")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
