package com.shortlink.admin.service;

import com.shortlink.admin.dto.CaptchaVO;
import com.shortlink.common.api.ResultCode;
import com.shortlink.common.constant.RedisKeys;
import com.shortlink.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 后台登录图形验证码：服务端用 JDK 自带的 AWT 画图，不引额外依赖。
 *
 * <p>验证码存 Redis、5 分钟过期、校验后立即删除，防止重复使用。</p>
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final char[] CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private static final int WIDTH = 120;

    private static final int HEIGHT = 40;

    private static final int CODE_LENGTH = 4;

    private final StringRedisTemplate redisTemplate;

    public CaptchaVO generate() {
        String code = randomCode();
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(RedisKeys.adminCaptcha(captchaId), code, Duration.ofMinutes(5));
        return new CaptchaVO(captchaId, "data:image/png;base64," + render(code));
    }

    /**
     * 校验后立即作废，避免同一个验证码被反复提交。
     */
    public void verify(String captchaId, String input) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(input)) {
            throw new BizException(ResultCode.PARAM_ERROR, "请输入图形验证码");
        }
        String key = RedisKeys.adminCaptcha(captchaId);
        String cached = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        if (cached == null || !cached.equalsIgnoreCase(input.trim())) {
            throw new BizException(ResultCode.PARAM_ERROR, "验证码错误或已过期");
        }
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS[ThreadLocalRandom.current().nextInt(CHARS.length)]);
        }
        return sb.toString();
    }

    private String render(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(247, 248, 250));
            graphics.fillRect(0, 0, WIDTH, HEIGHT);

            for (int i = 0; i < 6; i++) {
                graphics.setColor(lightColor());
                graphics.drawLine(ThreadLocalRandom.current().nextInt(WIDTH),
                        ThreadLocalRandom.current().nextInt(HEIGHT),
                        ThreadLocalRandom.current().nextInt(WIDTH),
                        ThreadLocalRandom.current().nextInt(HEIGHT));
            }

            graphics.setFont(new Font("Arial", Font.BOLD, 24));
            for (int i = 0; i < code.length(); i++) {
                graphics.setColor(darkColor());
                graphics.drawString(String.valueOf(code.charAt(i)), 14 + i * 26,
                        28 + ThreadLocalRandom.current().nextInt(6) - 3);
            }
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("生成验证码图片失败", e);
        }
    }

    private Color lightColor() {
        return new Color(200 + ThreadLocalRandom.current().nextInt(40),
                200 + ThreadLocalRandom.current().nextInt(40),
                200 + ThreadLocalRandom.current().nextInt(40));
    }

    private Color darkColor() {
        return new Color(30 + ThreadLocalRandom.current().nextInt(90),
                30 + ThreadLocalRandom.current().nextInt(90),
                30 + ThreadLocalRandom.current().nextInt(90));
    }
}
