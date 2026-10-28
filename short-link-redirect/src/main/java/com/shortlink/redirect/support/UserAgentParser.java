package com.shortlink.redirect.support;

/**
 * 从 User-Agent 粗略提取「系统 · 浏览器」，用于访问明细展示。
 *
 * <p>只做轻量识别，不引第三方库：统计维度到「设备类型 + 浏览器」这一层已经够用。</p>
 */
public final class UserAgentParser {

    private UserAgentParser() {
    }

    public static String parse(String ua) {
        if (ua == null || ua.isEmpty()) {
            return "未知";
        }
        return os(ua) + " · " + browser(ua);
    }

    private static String os(String ua) {
        if (ua.contains("iPhone") || ua.contains("iPad")) {
            return "iOS";
        }
        if (ua.contains("Android")) {
            return "Android";
        }
        if (ua.contains("Windows")) {
            return "Windows";
        }
        if (ua.contains("Macintosh") || ua.contains("Mac OS X")) {
            return "macOS";
        }
        if (ua.contains("Linux")) {
            return "Linux";
        }
        return "未知";
    }

    private static String browser(String ua) {
        if (ua.contains("MicroMessenger")) {
            return "微信";
        }
        if (ua.contains("Edg/")) {
            return "Edge";
        }
        if (ua.contains("Chrome/")) {
            return "Chrome";
        }
        if (ua.contains("Safari/")) {
            return "Safari";
        }
        if (ua.contains("Firefox/")) {
            return "Firefox";
        }
        return "未知";
    }
}
