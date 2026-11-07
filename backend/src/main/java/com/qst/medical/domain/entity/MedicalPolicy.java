package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 医保政策实体，对应 digital_medical_application_system.medical_policy。
 *
 * <p>注意：这张表的 create_time / update_time 是 varchar(255)，不是 datetime，
 * 所以这里用 String 承接，避免 MyBatis 类型转换失败。</p>
 */
@Data
public class MedicalPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String message;
    /** 所属城市 id，一条医保政策对应一个城市 */
    private Long cityId;
    private String createTime;
    private String updateTime;
}
