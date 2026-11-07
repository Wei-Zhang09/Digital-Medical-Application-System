package com.qst.medical.domain.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 操作日志：记录谁在什么时候对哪个模块做了什么。
 *
 * <p>只记录「写操作」（新增/修改/删除/重置口令/导出），查询不记，避免日志表被刷爆。</p>
 */
@Getter
@Setter
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 操作人登录名 */
    private String uname;

    /** 操作人姓名 */
    private String realname;

    /** 模块中文名，如「药品信息」 */
    private String module;

    /** 动作：新增 / 修改 / 删除 / 重置口令 / 导出 */
    private String action;

    /** 详情：请求方法 + 路径 + 简要参数 */
    private String detail;

    /** 来源 IP */
    private String ip;

    private Date createtime;
}
