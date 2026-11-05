package com.qst.medical.service;

import com.qst.medical.domain.model.PermissionModel;

import java.util.List;

/**
 * 菜单权限服务。
 */
public interface PermissionService {

    /**
     * 取某角色可见的菜单树（根节点为 Layout 与 404）。
     *
     * @param roleName 形如 ROLE_1 / ROLE_2
     * @return 菜单树；角色为空返回空列表
     */
    List<PermissionModel> getAllPermission(String roleName);
}
