package com.qst.medical.domain.entity;

import com.qst.medical.common.PageBean;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 用户实体类（第一层：与 account 表字段一一对应）。
 *
 * <p>继承 {@link PageBean} 以获得分页能力（官方任务 1 步骤 2）。</p>
 */
@Getter
@Setter
public class Account extends PageBean {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;
    /** 真实姓名 */
    private String realname;
    /** 登录名（唯一） */
    private String uname;
    /** 密码（BCrypt 密文） */
    private String pwd;
    /** 手机号 */
    private String phonenumber;
    /** 用户类型，即角色名：ROLE_1 管理员 / ROLE_2 医师 */
    private String utype;
    private Date createtime;
    private Date updatetime;
}
