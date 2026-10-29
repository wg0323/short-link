package com.shortlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "发送验证码结果")
public class SendSmsResult {

    /**
     * Mock 通道下直接回传验证码，方便本地联调；真实短信通道下该字段为 null。
     */
    @Schema(description = "Mock 通道直接回传的验证码；真实短信通道下为 null", example = "123456")
    private String mockCode;

    public SendSmsResult() {
    }

    public SendSmsResult(String mockCode) {
        this.mockCode = mockCode;
    }

    public String getMockCode() {
        return mockCode;
    }

    public void setMockCode(String mockCode) {
        this.mockCode = mockCode;
    }
}
