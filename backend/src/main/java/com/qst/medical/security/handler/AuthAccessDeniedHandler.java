package com.qst.medical.security.handler;

import com.qst.medical.common.Msg;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 无权限处理器：已登录但角色不够时返回 10000。
 *
 * <p>类名加 Auth 前缀是为了和 Spring Security 的 {@link AccessDeniedHandler} 接口区分
 * （同名类无法 import 接口）。</p>
 */
@Component
public class AuthAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        MsgWriter.write(response, Msg.fail("权限不足，无法访问该资源"));
    }
}
