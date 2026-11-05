package com.qst.medical.security.handler;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.dto.AccountDto;
import com.qst.medical.domain.model.AccountModel;
import com.qst.medical.security.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录成功处理器：签发 JWT 并把当前用户信息一起返回。
 *
 * <p>返回结构：{@code code=20000, data={token, tokenHead, utype, role, user}}，
 * 其中 utype 是短码（"1"/"2"），role 是完整角色名（ROLE_1/ROLE_2）。</p>
 */
@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AccountDto)) {
            MsgWriter.write(response, Msg.fail("登录失败：无法获取用户信息"));
            return;
        }
        AccountDto account = (AccountDto) principal;
        String role = account.getRole() != null ? account.getRole() : account.getUtype();
        String token = JwtUtil.getJwtToken(account.getId(), account.getUname(), role);
        AccountModel user = AccountModel.from(account);

        MsgWriter.write(response, Msg.success("登录成功")
                .data("token", token)
                .data("tokenHead", JwtUtil.TOKEN_PREFIX.trim())
                .data("utype", user.getUtype())
                .data("role", user.getRole())
                .data("user", user));
    }
}
