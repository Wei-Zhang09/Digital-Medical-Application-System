package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;

/** 医师级别（职称），对应 digital_medical_application_system.doctor_level。 */
@Data
public class DoctorLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
}
