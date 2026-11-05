package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.dto.AccountDto;
import com.qst.medical.domain.model.PermissionModel;
import com.qst.medical.service.PermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 菜单权限接口：登录后前端拿它渲染左侧菜单和动态路由。
 */
@Api(tags = "菜单权限")
@RestController
@RequestMapping("/api")
public class PermissionController {

    @Resource
    private PermissionService permissionService;

    /**
     * 当前登录用户可见的菜单树。
     *
     * <p>角色从 JWT 里取（JwtFilter 已放进 SecurityContext 的 principal），
     * 不信任前端传参，避免越权拿别人的菜单。</p>
     */
    @ApiOperation("获取当前用户的菜单树")
    @GetMapping("/permissions")
    public Msg getPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AccountDto)) {
            return Msg.unauthorized();
        }
        AccountDto account = (AccountDto) authentication.getPrincipal();
        List<PermissionModel> permissions = permissionService.getAllPermission(account.getRole());
        return Msg.success("获取菜单成功").data("permissions", permissions);
    }
}
