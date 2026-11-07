package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.param.RegisterParam;
import com.qst.medical.service.AccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
}
