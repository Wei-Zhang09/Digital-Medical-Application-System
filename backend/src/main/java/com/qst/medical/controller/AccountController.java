package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.dto.AccountDto;
import com.qst.medical.domain.param.PasswordParam;
import com.qst.medical.domain.param.RegisterParam;
import com.qst.medical.service.AccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;

/**
 * 账号相关接口：自助注册。
 *
 * <p>登录仍由 Spring Security 的 formLogin 处理（/api/login），这里不重复实现。</p>
 */
@Api(tags = "账号管理")
@RestController
@RequestMapping("/api")
public class AccountController {

    @Resource
    private AccountService accountService;

    @ApiOperation("用户注册（免登录）")
    @PostMapping("/register")
    public Msg register(@RequestBody @Validated RegisterParam param) {
        accountService.register(param);
        return Msg.success("注册成功，请使用新账号登录");
    }

    /**
     * 修改当前登录用户的口令。
     *
     * <p>用户身份从 SecurityContext 取（JwtFilter 放进去的 principal），
     * 不接受前端传 id，避免改别人的密码。</p>
     */
    @ApiOperation("修改密码（当前登录用户）")
    @PostMapping("/account/password")
    @RolesAllowed({"ROLE_1", "ROLE_2"})
    public Msg changePassword(@RequestBody @Validated PasswordParam param) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AccountDto)) {
            return Msg.unauthorized();
        }
        AccountDto account = (AccountDto) authentication.getPrincipal();
        accountService.changePassword(account.getId(), param);
        return Msg.success("密码修改成功，请重新登录");
    }
}
