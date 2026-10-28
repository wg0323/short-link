package com.shortlink.common.util;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * IP 相关工具：取真实客户端 IP、判断是否为内网/保留地址。
 */
public final class IpUtils {

    private IpUtils() {
    }

    /**
     * 取客户端 IP，优先取代理头。取到的一律只保留第一个地址。
     */
    public static String getClientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value)) {
                int comma = value.indexOf(',');
                return (comma > 0 ? value.substring(0, comma) : value).trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 判断 IP 字面量是否属于内网、回环、链路本地、组播等不可对外访问的段。
     * 无法解析时按不安全处理。
     */
    public static boolean isInternalLiteral(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        String normalized = ip;
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        try {
            return isInternalAddress(InetAddress.getByName(normalized));
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isInternalAddress(InetAddress address) {
        if (address == null || address.isAnyLocalAddress() || address.isLoopbackAddress()
                || address.isLinkLocalAddress() || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }
        byte[] bytes = address.getAddress();
        if (address instanceof Inet4Address) {
            int first = bytes[0] & 0xFF;
            int second = bytes[1] & 0xFF;
            // 0.0.0.0/8、100.64.0.0/10（运营商 NAT）、169.254.0.0/16、192.0.0.0/24 等保留段
            if (first == 0 || (first == 100 && second >= 64 && second <= 127)
                    || (first == 169 && second == 254) || (first == 192 && second == 0)) {
                return true;
            }
            return false;
        }
        if (address instanceof Inet6Address) {
            int first = bytes[0] & 0xFF;
            // fc00::/7 唯一本地地址、fe80::/10 链路本地
            return (first & 0xFE) == 0xFC || (first == 0xFE && (bytes[1] & 0xC0) == 0x80);
        }
        return true;
    }

    /**
     * 返回脱敏后的 IP，用于统计明细展示。
     */
    public static String mask(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "";
        }
        if (ip.indexOf(':') >= 0) {
            int idx = ip.lastIndexOf(':');
            return ip.substring(0, Math.max(idx, 0)) + ":*";
        }
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return ip;
        }
        return parts[0] + "." + parts[1] + ".*.*";
    }
}
