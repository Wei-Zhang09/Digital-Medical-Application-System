package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医药公司实体，对应 digital_medical_application_system.drugcompany。
 */
@Data
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long companyId;
    private String companyName;
    private String companyPhone;
    private LocalDateTime createtime;
    private LocalDateTime updatetime;
}
