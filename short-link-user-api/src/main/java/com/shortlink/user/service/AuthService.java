package com.shortlink.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortlink.common.api.ResultCode;
import com.shortlink.common.constant.RedisKeys;
import com.shortlink.common.exception.BizException;
import com.shortlink.common.security.JwtService;
import com.shortlink.user.dto.AuthResponse;
import com.shortlink.user.dto.LoginRequest;
import com.shortlink.user.dto.RegisterRequest;
import com.shortlink.user.dto.SendSmsResult;
import com.shortlink.user.entity.User;
import com.shortlink.user.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 注册与登录。
 *
 * <p>安全要点：
 * <ul>
 *   <li>验证码存 Redis，5 分钟过期，用后即焚；</li>
 *   <li>同一手机号 60 秒内只能发一次，每天上限 10 次；</li>
 *   <li>连续 5 次密码错误锁定 15 分钟，防暴力破解；</li>
 *   <li>手机号唯一索引兜底并发注册，重复插入转成业务提示。</li>
 * </ul></p>
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final int SMS_DAILY_LIMIT = 10;

    private static final int LOGIN_FAIL_LIMIT = 5;

    private static final String MOCK_CODE = "123456";

    /** 旧刷新令牌的宽限期，避开多标签页同时刷新时的自我踢下线。 */
    private static final long REFRESH_GRACE_SECONDS = 60L;

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SmsSender smsSender;
    private final AvatarStorage avatarStorage;
    private final long codeExpireSeconds;
    private final long refreshSeconds;
    private final boolean mock;

    public AuthService(UserMapper userMapper,
                       StringRedisTemplate redisTemplate,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       SmsSender smsSender,
                       AvatarStorage avatarStorage,
                       @Value("${shortlink.sms.code-expire-seconds:300}") long codeExpireSeconds,
                       @Value("${shortlink.sms.mock:true}") boolean mock,
                       @Value("${shortlink.jwt.refresh-seconds:604800}") long refreshSeconds) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.smsSender = smsSender;
        this.avatarStorage = avatarStorage;
        this.codeExpireSeconds = codeExpireSeconds;
        this.mock = mock;
        this.refreshSeconds = refreshSeconds;
    }

    public SendSmsResult sendSms(String phone) {
        String dailyKey = RedisKeys.smsDailyCount(phone, LocalDate.now().toString());
        Long count = redisTemplate.opsForValue().increment(dailyKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(dailyKey, Duration.ofDays(1));
        }
        if (count != null && count > SMS_DAILY_LIMIT) {
            throw new BizException(ResultCode.SMS_SEND_TOO_FREQUENT, "今日验证码发送次数已达上限");
        }

        String lockKey = RedisKeys.smsSendLock(phone);
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(60));
        if (acquired == null || !acquired) {
            throw new BizException(ResultCode.SMS_SEND_TOO_FREQUENT);
        }

        String code = mock ? MOCK_CODE
                : String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        redisTemplate.opsForValue().set(RedisKeys.smsCode(phone), code, Duration.ofSeconds(codeExpireSeconds));
        smsSender.send(phone, code);

        return new SendSmsResult(mock ? code : null);
    }

    public AuthResponse register(RegisterRequest request) {
        String cachedCode = redisTemplate.opsForValue().get(RedisKeys.smsCode(request.getPhone()));
        if (cachedCode == null || !cachedCode.equals(request.getCode())) {
            throw new BizException(ResultCode.SMS_CODE_ERROR);
        }

        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (exists != null && exists > 0) {
            throw new BizException(ResultCode.PHONE_REGISTERED);
        }

        User user = new User();
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(buildNickname(request.getNickname(), request.getPhone()));
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.PHONE_REGISTERED);
        }

        redisTemplate.delete(RedisKeys.smsCode(request.getPhone()));
        return issueToken(user);
    }

    public AuthResponse login(LoginRequest request) {
        String failKey = RedisKeys.loginFail(request.getPhone());
        String failValue = redisTemplate.opsForValue().get(failKey);
        if (failValue != null && Integer.parseInt(failValue) >= LOGIN_FAIL_LIMIT) {
            throw new BizException(ResultCode.RATE_LIMITED, "密码错误次数过多，请 15 分钟后再试");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            Long fails = redisTemplate.opsForValue().increment(failKey);
            if (fails != null && fails == 1L) {
                redisTemplate.expire(failKey, Duration.ofMinutes(15));
            }
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }

        redisTemplate.delete(failKey);
        User update = new User();
        update.setId(user.getId());
        update.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(update);
        return issueToken(user);
    }

    public AuthResponse refresh(String refreshToken) {
        long userId;
        try {
            userId = jwtService.parseSubjectId(refreshToken);
        } catch (BizException e) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        String currentKey = RedisKeys.refreshToken(userId);
        String current = redisTemplate.opsForValue().get(currentKey);
        if (current == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }

        if (!current.equals(refreshToken)) {
            // 宽限期内接受上一个令牌：多标签页同时刷新时，落后的一方拿旧令牌换到的是同一个当前令牌
            String previous = redisTemplate.opsForValue().get(RedisKeys.refreshTokenPrevious(userId));
            if (previous == null || !previous.equals(refreshToken)) {
                throw new BizException(ResultCode.UNAUTHORIZED);
            }
            return new AuthResponse(jwtService.createToken(userId, "user"), current,
                    jwtService.getExpireSeconds(), toProfile(user));
        }

        // 轮换：旧令牌降级为「宽限期可用」，此后必须用新令牌
        String nextRefreshToken = jwtService.createToken(userId, "refresh", refreshSeconds);
        redisTemplate.opsForValue().set(RedisKeys.refreshTokenPrevious(userId), current,
                Duration.ofSeconds(REFRESH_GRACE_SECONDS));
        redisTemplate.opsForValue().set(currentKey, nextRefreshToken, Duration.ofSeconds(refreshSeconds));
        return new AuthResponse(jwtService.createToken(userId, "user"), nextRefreshToken,
                jwtService.getExpireSeconds(), toProfile(user));
    }

    public void logout(long userId) {
        redisTemplate.delete(RedisKeys.refreshToken(userId));
        redisTemplate.delete(RedisKeys.refreshTokenPrevious(userId));
        log.info("用户登出 userId={}", userId);
    }

    private AuthResponse issueToken(User user) {
        String accessToken = jwtService.createToken(user.getId(), "user");
        String refreshToken = jwtService.createToken(user.getId(), "refresh", refreshSeconds);
        // 新会话不继承上一轮留下的宽限期旧令牌
        redisTemplate.delete(RedisKeys.refreshTokenPrevious(user.getId()));
        redisTemplate.opsForValue().set(RedisKeys.refreshToken(user.getId()), refreshToken,
                Duration.ofSeconds(refreshSeconds));
        return new AuthResponse(accessToken, refreshToken, jwtService.getExpireSeconds(), toProfile(user));
    }

    private AuthResponse.Profile toProfile(User user) {
        return new AuthResponse.Profile(user.getId(), user.getPhone(), user.getNickname(),
                avatarStorage.urlOf(user.getAvatar()), user.getCreatedAt());
    }

    private String buildNickname(String nickname, String phone) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname.trim();
        }
        return "用户" + phone.substring(phone.length() - 4);
    }
}
