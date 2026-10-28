package com.shortlink.common.security;

import com.shortlink.common.api.ResultCode;
import com.shortlink.common.exception.BizException;
import com.shortlink.common.util.IpUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 长链接安全校验。
 *
 * <p>短链平台最大的服务端请求伪造（SSRF）面在于「接受用户提交的任意 URL 后由服务端访问它」。
 * 本系统的跳转端只返回 302，不发起任何外呼，因此 SSRF 校验集中在生成入口：
 * 协议白名单、拒绝 userinfo、端口白名单、域名解析后校验实际 IP 是否内网、拒绝自引用。</p>
 */
@Component
public class UrlSecurityValidator {

    private static final int MAX_LENGTH = 2048;

    private static final List<String> ALLOWED_SCHEMES = Arrays.asList("http", "https");

    private static final List<Integer> ALLOWED_PORTS = Arrays.asList(-1, 80, 443, 8080);

    private final List<String> selfHosts;

    public UrlSecurityValidator(@Value("${shortlink.link.self-hosts:}") String selfHosts) {
        this.selfHosts = new ArrayList<>();
        if (selfHosts != null) {
            for (String host : selfHosts.split(",")) {
                String trimmed = host.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    this.selfHosts.add(trimmed);
                }
            }
        }
    }

    /**
     * 校验并返回解析后的 URI，不合法时抛出业务异常。
     */
    public URI validate(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            throw new BizException(ResultCode.URL_ILLEGAL, "长链接不能为空");
        }
        String url = rawUrl.trim();
        if (url.length() > MAX_LENGTH) {
            throw new BizException(ResultCode.URL_ILLEGAL, "长链接长度不能超过 " + MAX_LENGTH + " 个字符");
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new BizException(ResultCode.URL_ILLEGAL, "长链接格式不正确");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new BizException(ResultCode.URL_ILLEGAL, "仅支持 http / https 协议的链接");
        }
        if (uri.getUserInfo() != null) {
            throw new BizException(ResultCode.URL_ILLEGAL, "长链接不能包含用户凭证信息");
        }
        if (!ALLOWED_PORTS.contains(uri.getPort())) {
            throw new BizException(ResultCode.URL_ILLEGAL, "长链接端口不合法");
        }

        String host = uri.getHost();
        if (host == null || host.trim().isEmpty()) {
            throw new BizException(ResultCode.URL_ILLEGAL, "长链接缺少有效域名");
        }
        host = host.toLowerCase();

        for (String selfHost : selfHosts) {
            if (host.equals(selfHost) || host.endsWith("." + selfHost)) {
                throw new BizException(ResultCode.URL_ILLEGAL, "不能把本平台域名作为跳转目标");
            }
        }

        checkHostAddress(host);
        return uri;
    }

    /**
     * 域名解析后逐个校验实际 IP，防止用域名指向内网绕过字面量检查。
     */
    private void checkHostAddress(String host) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            if (addresses.length == 0) {
                throw new BizException(ResultCode.URL_ILLEGAL, "无法解析该域名");
            }
            for (InetAddress address : addresses) {
                if (IpUtils.isInternalAddress(address)) {
                    throw new BizException(ResultCode.URL_ILLEGAL, "长链接不能指向内网或保留地址");
                }
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ResultCode.URL_ILLEGAL, "无法解析该域名");
        }
    }
}
