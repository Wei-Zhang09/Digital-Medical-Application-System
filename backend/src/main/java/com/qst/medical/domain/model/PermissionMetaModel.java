package com.qst.medical.domain.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 菜单的 meta 信息，对应 vue-router 路由的 meta 字段。
 */
@Data
public class PermissionMetaModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 菜单显示名（医师角色下「管理」会被替换为「查询」） */
    private String title;

    /** 菜单图标 */
    private String icon;
}
