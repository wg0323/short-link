package com.shortlink.common.security;

import com.shortlink.common.api.ResultCode;
import com.shortlink.common.exception.BizException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析。
 *
 * <p>用户端与后台管理使用两套独立密钥（不同的 {@code shortlink.jwt.secret}），
 * 后台令牌无法访问用户接口，反之亦然。</p>
 */
@Component
@ConditionalOnProperty(prefix = "shortlink.jwt", name = "secret")
public class JwtService {

    private final SecretKey key;

    private final long expireSeconds;

    public JwtService(@Value("${shortlink.jwt.secret}") String secret,
                      @Value("${shortlink.jwt.expire-seconds:7200}") long expireSeconds) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("shortlink.jwt.secret 至少需要 32 个字节");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expireSeconds = expireSeconds;
    }

    public String createToken(long subjectId, String role) {
        return createToken(subjectId, role, expireSeconds);
    }

    /**
     * 指定有效期签发，刷新令牌这类长时间凭证用得到。
     */
    public String createToken(long subjectId, String role, long ttlSeconds) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ttlSeconds * 1000L);
        return Jwts.builder()
                .setSubject(String.valueOf(subjectId))
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public long parseSubjectId(String token) {
        return parse(token).getSubjectId();
    }

    /**
     * 解析令牌中的主体与角色；角色用于区分用户端与后台的载荷。
     */
    public JwtPrincipal parse(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String role = claims.get("role", String.class);
            return new JwtPrincipal(Long.parseLong(claims.getSubject()), role);
        } catch (Exception e) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }
}
