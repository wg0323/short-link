package com.shortlink.common.util;

/**
 * Base62 编码：短码字符集为 0-9A-Za-z，定长输出，左补 '0'。
 */
public final class Base62 {

    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private static final int RADIX = ALPHABET.length;

    private static final String CODE_PATTERN = "^[0-9a-zA-Z]{5,8}$";

    private Base62() {
    }

    /**
     * 把自增长整型转为定长 Base62 字符串。
     */
    public static String encode(long value, int length) {
        if (value < 0) {
            throw new IllegalArgumentException("value must not be negative");
        }
        char[] buf = new char[length];
        for (int i = length - 1; i >= 0; i--) {
            buf[i] = ALPHABET[(int) (value % RADIX)];
            value /= RADIX;
        }
        if (value > 0) {
            throw new IllegalArgumentException("value is too large for length " + length);
        }
        return new String(buf);
    }

    /**
     * 短码是否满足平台字符集与长度要求。
     */
    public static boolean isValidCode(String code) {
        return code != null && code.matches(CODE_PATTERN);
    }
}
