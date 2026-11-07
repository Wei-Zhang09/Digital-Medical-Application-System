package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医药公司政策实体，对应 digital_medical_application_system.company_policy。
 */
@Data
public class CompanyPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String message;
    /** 所属医药公司 id */
    private Long companyId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
