package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.dto.AccountDto;
import com.qst.medical.domain.param.AccountParam;
import com.qst.medical.domain.param.PasswordParam;
import com.qst.medical.domain.param.RegisterParam;
import com.qst.medical.service.AccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Min;

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

    // ==================== 账号管理（仅管理员） ====================

    @ApiOperation("账号分页列表（管理员）")
    @GetMapping("/accounts/{pageNum}/{pageSize}")
    @RolesAllowed({"ROLE_1"})
    public Msg getAccountList(@PathVariable @Min(1) Integer pageNum,
                              @PathVariable @Min(1) Integer pageSize,
                              @RequestParam(required = false) String keyword) {
        return Msg.success().data("accountPageInfo",
                accountService.getAccountList(pageNum, pageSize, keyword));
    }

    @ApiOperation("新增账号（管理员，初始口令 123456）")
    @PostMapping("/accounts")
    @RolesAllowed({"ROLE_1"})
    public Msg addAccount(@RequestBody @Validated AccountParam param) {
        accountService.addAccount(param);
        return Msg.success("新增成功，初始口令 123456");
    }

    @ApiOperation("修改账号（管理员）")
    @PutMapping("/accounts/{id}")
    @RolesAllowed({"ROLE_1"})
    public Msg updateAccount(@PathVariable Long id, @RequestBody @Validated AccountParam param) {
        accountService.updateAccount(id, param);
        return Msg.success("修改成功");
    }

    @ApiOperation("重置账号口令为 123456（管理员）")
    @PutMapping("/accounts/{id}/password")
    @RolesAllowed({"ROLE_1"})
    public Msg resetAccountPassword(@PathVariable Long id) {
        accountService.resetPassword(id);
        return Msg.success("口令已重置为 123456");
    }

    @ApiOperation("删除账号（管理员）")
    @DeleteMapping("/accounts/{id}")
    @RolesAllowed({"ROLE_1"})
    public Msg deleteAccount(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentId = null;
        if (authentication != null && authentication.getPrincipal() instanceof AccountDto) {
            currentId = ((AccountDto) authentication.getPrincipal()).getId();
        }
        accountService.deleteAccount(id, currentId);
        return Msg.success("删除成功");
    }
}
