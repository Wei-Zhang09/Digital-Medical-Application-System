package com.qst.medical.service;

import com.qst.medical.domain.dto.AccountDto;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户服务：负责登录用户校验与角色获取。
 *
 * <p>继承 {@link UserDetailsService}，直接交给 Spring Security 的 DaoAuthenticationProvider 使用，
 * 登录接口 /api/login 由 formLogin 处理，不需要单独写 Controller。</p>
 */
public interface AccountService extends UserDetailsService {

    /**
     * 按登录名查询用户（含密码与角色），供认证与 JwtFilter 复用。
     *
     * @param uname 登录名
     * @return 用户，不存在返回 null
     */
    AccountDto getAccountByUname(String uname);
}
