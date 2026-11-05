package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 销售地点（药店）实体，对应 bin_text.sale。
 *
 * <p>扩展功能「销售地点地图」会在本表加 lng / lat / address 三列（见 sql/sale_ext.sql），
 * 那三个字段在 Task 10 一起补，这里先保持基础字段。</p>
 */
@Data
public class Sale implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long saleId;
    private String saleName;
    private String salePhone;
    private LocalDateTime createtime;
    private LocalDateTime updatetime;
}
