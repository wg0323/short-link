package com.shortlink.user.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor;
import com.shortlink.common.security.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：登录拦截器与开发期跨域。
 *
 * <p>白名单之外的 {@code /api/**} 全部要求携带 JWT；认证相关的匿名接口在这里放行。</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String[] WHITE_LIST = {
            "/api/auth/sms/send",
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/actuator/**"
    };

    private final JwtAuthInterceptor jwtAuthInterceptor;

    /** 接入 Sentinel 的接口：都是路径固定、无 path 变量的。 */
    private static final String[] SENTINEL_PATHS = {
            "/api/link/generate",
            "/api/stat/**",
            "/api/user/avatar"
    };

    public WebConfig(JwtAuthInterceptor jwtAuthInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 只挂在路径固定的接口上，避免 /api/link/{code} 这类路径被拆成一堆资源
        registry.addInterceptor(new SentinelWebInterceptor())
                .addPathPatterns(SENTINEL_PATHS);
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(WHITE_LIST);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
