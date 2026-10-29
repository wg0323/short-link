package com.shortlink.common.exception;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.shortlink.common.api.Result;
import com.shortlink.common.api.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理：业务异常按原状态码返回，其余统一 500。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBiz(BizException e) {
        return ResponseEntity.status(toHttpStatus(e.getResultCode()))
                .body(Result.fail(e.getResultCode(), e.getMessage()));
    }

    /**
     * 认证、越权、限流用真实 HTTP 状态码，其余业务错误保持 200 + 业务码，
     * 前端可以用一个拦截器分别处理「需要登录」和「业务提示」。
     */
    private HttpStatus toHttpStatus(ResultCode code) {
        switch (code) {
            case UNAUTHORIZED:
                return HttpStatus.UNAUTHORIZED;
            case FORBIDDEN:
            case ACCOUNT_DISABLED:
            case LINK_EXPIRED:
            case LINK_DISABLED:
                return HttpStatus.FORBIDDEN;
            case RATE_LIMITED:
                return HttpStatus.TOO_MANY_REQUESTS;
            default:
                return HttpStatus.OK;
        }
    }

    @ExceptionHandler(BlockException.class)
    public ResponseEntity<Result<Void>> handleBlock(BlockException e) {
        log.warn("请求被 Sentinel 拦截 resource={} rule={}", e.getRule() == null ? "-" : e.getRule().getResource(),
                e.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Result.fail(ResultCode.RATE_LIMITED, "请求过于频繁，请稍后再试"));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValid(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? ResultCode.PARAM_ERROR.getMessage() : fieldError.getDefaultMessage();
        return Result.fail(ResultCode.PARAM_ERROR, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraint(ConstraintViolationException e) {
        String message = ResultCode.PARAM_ERROR.getMessage();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            message = violation.getMessage();
            break;
        }
        return Result.fail(ResultCode.PARAM_ERROR, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("未处理异常", e);
        return Result.fail(ResultCode.SYSTEM_ERROR);
    }
}
