package com.shortlink.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mock 短信通道：不调用任何外部服务，只把验证码打到日志。
 *
 * <p>真实接入时新增一个 {@link SmsSender} 实现并把 {@code shortlink.sms.mock} 置为 false。</p>
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "shortlink.sms", name = "mock", havingValue = "true", matchIfMissing = true)
public class MockSmsSender implements SmsSender {

    @Override
    public void send(String phone, String code) {
        log.info("[MockSms] 发送验证码 phone={} code={}", maskPhone(phone), code);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
