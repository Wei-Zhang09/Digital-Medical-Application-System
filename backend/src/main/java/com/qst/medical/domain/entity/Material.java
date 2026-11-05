package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 必备材料实体，对应 bin_text.material。
 */
@Data
public class Material implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String title;
    private String message;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
