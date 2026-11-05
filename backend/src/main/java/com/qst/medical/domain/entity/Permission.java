package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 权限（菜单）表实体，对应 bin_text.permission。
 *
 * <p>一条记录就是前端的一个菜单项：{@code path} 是路由地址，{@code component} 是对应的 Vue 组件名，
 * {@code level} 0 表示根节点（Layout / 404），1 表示业务菜单。</p>
 */
@Data
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 权限 id */
    private Integer id;

    /** 父 id，0 为根 */
    private Integer pid;

    /** 路由 name */
    private String name;

    /** 路由 path */
    private String path;

    /** 前端组件名 */
    private String component;

    /** 菜单级别：0 根，1 业务菜单 */
    private Integer level;

    /** 菜单标题 */
    private String title;
}
