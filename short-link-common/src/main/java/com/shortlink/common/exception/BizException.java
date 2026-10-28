package com.shortlink.common.exception;

import com.shortlink.common.api.ResultCode;

/**
 * 业务异常：由 {@code GlobalExceptionHandler} 转成统一响应。
 */
public class BizException extends RuntimeException {

    private final ResultCode resultCode;

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}
