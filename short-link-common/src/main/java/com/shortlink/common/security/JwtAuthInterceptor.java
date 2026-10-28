package com.shortlink.common.security;

import com.shortlink.common.api.ResultCode;
import com.shortlink.common.constant.RedisKeys;
import com.shortlink.common.exception.BizException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器：校验 Authorization 头中的 JWT，并把用户 ID 放进 {@link UserContext}。
 *
 * <p>只有配置了 {@code shortlink.jwt.secret} 的服务才会注册本拦截器，
 * 跳转服务不依赖 JWT，因此不会被创建。</p>
 */
@Component
@ConditionalOnProperty(prefix = "shortlink.jwt", name = "secret")
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final String HEADER = "Authorization";

    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    private final StringRedisTemplate redisTemplate;

    public JwtAuthInterceptor(JwtService jwtService, StringRedisTemplate redisTemplate) {
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        JwtPrincipal principal = jwtService.parse(header.substring(PREFIX.length()).trim());
        UserContext.set(principal.getSubjectId());
        // 只对用户端令牌检查禁用标记：后台账号与用户在各自的 ID 空间，不能共用这个 key
        if (principal.isUser()
                && Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeys.disabledUser(principal.getSubjectId())))) {
            UserContext.clear();
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
