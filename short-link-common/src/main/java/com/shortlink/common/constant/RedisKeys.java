package com.shortlink.common.constant;

/**
 * Redis key 统一收口，避免各服务拼错前缀。
 */
public final class RedisKeys {

    private static final String PREFIX = "sl:";

    /** 短链缓存失效广播频道。 */
    public static final String LINK_EVICT_CHANNEL = PREFIX + "link:evict";

    /** 新建短链广播频道：跳转端收到后把短码加入布隆过滤器。 */
    public static final String LINK_ADD_CHANNEL = PREFIX + "link:add";

    private RedisKeys() {
    }

    public static String rateLimit(String name, String dimension) {
        return PREFIX + "rl:" + name + ":" + dimension;
    }

    public static String smsCode(String phone) {
        return PREFIX + "sms:code:" + phone;
    }

    public static String smsSendLock(String phone) {
        return PREFIX + "sms:lock:" + phone;
    }

    public static String smsDailyCount(String phone, String date) {
        return PREFIX + "sms:daily:" + date + ":" + phone;
    }

    public static String refreshToken(long userId) {
        return PREFIX + "auth:refresh:" + userId;
    }

    /**
     * 轮换前的旧刷新令牌，只保留一个宽限期。
     * 多标签页同时刷新时，落后一拍的那个用旧令牌仍能换到当前令牌，不会被踢下线。
     */
    public static String refreshTokenPrevious(long userId) {
        return PREFIX + "auth:refresh:prev:" + userId;
    }

    /** 登录失败计数，用于密码爆破保护。 */
    public static String loginFail(String phone) {
        return PREFIX + "auth:fail:" + phone;
    }

    /**
     * 被后台禁用的用户标记。用户端拦截器每次请求检查一次，
     * 保证禁用立即生效，而不用等令牌过期。
     */
    public static String disabledUser(long userId) {
        return PREFIX + "auth:disabled:" + userId;
    }

    /** 后台登录图形验证码。 */
    public static String adminCaptcha(String captchaId) {
        return PREFIX + "admin:captcha:" + captchaId;
    }

    /**
     * 用户累计点击计数（配额用）。
     * 只做实时值，权威值在 user_quota，由每日对账写回。
     */
    public static String quotaClickTotal(long userId) {
        return PREFIX + "q:click:" + userId;
    }

    /** 短链跳转缓存：sl:link:{code}。 */
    public static String link(String code) {
        return PREFIX + "link:" + code;
    }

    /** 某短链某天的 PV 计数。 */
    public static String statPv(String code, String date) {
        return PREFIX + "st:pv:" + date + ":" + code;
    }

    /** 某短链某天的 UV HyperLogLog。 */
    public static String statUv(String code, String date) {
        return PREFIX + "st:uv:" + date + ":" + code;
    }

    /** 某短链某天的独立 IP HyperLogLog。 */
    public static String statIp(String code, String date) {
        return PREFIX + "st:ip:" + date + ":" + code;
    }

    /** 某短链某天的地域计数 Hash，field 为省份。 */
    public static String statRegion(String code, String date) {
        return PREFIX + "st:region:" + date + ":" + code;
    }

    /** 某天产生过访问的短码集合，聚合任务遍历用。 */
    public static String statCodes(String date) {
        return PREFIX + "st:codes:" + date;
    }
}
