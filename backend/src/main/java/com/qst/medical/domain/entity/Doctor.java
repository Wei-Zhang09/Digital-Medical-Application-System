package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医师实体，对应 digital_medical_application_system.doctor。
 *
 * <p>医师信息与登录账号（account）是 1:1：新增医师时同步创建账号，
 * 删除医师时一并删除账号，{@code accountId} 是这条关联。</p>
 */
@Data
public class Doctor implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    /** 姓名 */
    private String name;
    private Integer age;
    /** 性别：1 男，2 女 */
    private Integer sex;
    /** 医师级别 id（doctor_level） */
    private Long levelId;
    private String phone;
    /** 诊治类型 id（treat_type） */
    private Long typeId;
    private String hospital;
    /** 关联的登录账号 id */
    private Long accountId;
    private LocalDateTime createtime;
    private LocalDateTime updatetime;

    /** 展示用：级别名与类型名（联表查询带出来，不入库） */
    private String levelName;
    private String typeName;
    /** 展示用：登录名 */
    private String uname;
}
