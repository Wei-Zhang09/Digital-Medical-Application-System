package com.qst.medical.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把业务异常统一转成 {@link Msg} 的 10000，
 * 前端只需要判断 code 就能给出提示，不用区分 500/400。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务校验失败（如「记录不存在」） */
    @ExceptionHandler(IllegalArgumentException.class)
    public Msg handleIllegalArgument(IllegalArgumentException e) {
        return Msg.fail(e.getMessage());
    }

    /**
     * 方法级鉴权失败（@RolesAllowed / @PreAuthorize）。
     *
     * <p>@RolesAllowed 抛出的 AccessDeniedException 发生在 Controller 方法内部，
     * 会先被这里接住；必须显式处理，否则会被下面的兜底当成「系统异常」。</p>
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public Msg handleAccessDenied(org.springframework.security.access.AccessDeniedException e) {
        return Msg.fail("权限不足，无法访问该资源");
    }

    /** @Validated 校验失败，取第一条字段错误 */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Msg handleValidException(BindException e) {
        FieldError error = e.getBindingResult().getFieldError();
        String message = error == null ? "参数校验失败" : error.getDefaultMessage();
        return Msg.fail(message);
    }

    /** 兜底：记录日志，返回统一失败体 */
    @ExceptionHandler(Exception.class)
    public Msg handleException(Exception e) {
        log.error("未处理异常", e);
        return Msg.fail("系统异常，请稍后再试");
    }
}
