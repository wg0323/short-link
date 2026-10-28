package com.shortlink.user.dto;

public class SendSmsResult {

    /**
     * Mock 通道下直接回传验证码，方便本地联调；真实短信通道下该字段为 null。
     */
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
