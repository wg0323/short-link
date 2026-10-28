package com.shortlink.user.service;

/**
 * 短信发送通道。一期只提供 Mock 实现，接入阿里云/腾讯云时新增一个实现类即可。
 */
public interface SmsSender {

    void send(String phone, String code);
}
