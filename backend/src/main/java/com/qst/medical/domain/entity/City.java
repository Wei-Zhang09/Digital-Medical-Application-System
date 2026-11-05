package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 城市实体，对应 bin_text.city。
 */
@Data
public class City implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long cityId;
    /** 城市编号（业务编号，与主键区分） */
    private Integer cityNumber;
    private LocalDateTime createtime;
    private LocalDateTime updatetime;
}
