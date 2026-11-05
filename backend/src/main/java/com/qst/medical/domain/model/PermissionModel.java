package com.qst.medical.domain.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点，直接给前端 routeParse 用。
 */
@Data
public class PermissionModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    /** 父 id */
    private Integer pid;

    /** 路由 name */
    private String name;

    /** 路由 path */
    private String path;

    /** 前端组件名 */
    private String component;

    /** 菜单级别 */
    private Integer level;

    /** 标题（同时放到 meta.title，前端两种写法都兼容） */
    private String title;

    /** 路由 meta */
    private PermissionMetaModel meta;

    /** 子菜单 */
    private List<PermissionModel> children = new ArrayList<>();
}
