package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;

/** 诊治类型（科室），对应 bin_text.treat_type。 */
@Data
public class TreatType implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
}
