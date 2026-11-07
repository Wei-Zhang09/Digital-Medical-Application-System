package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 销售地点（药店）实体，对应 digital_medical_application_system.sale。
 *
 * <p>lng / lat / address 是扩展功能「销售地点地图」加的字段（见 sql/sale_ext.sql）。</p>
 */
@Data
public class Sale implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long saleId;
    private String saleName;
    private String salePhone;
    /** 经度 */
    private Double lng;
    /** 纬度 */
    private Double lat;
    /** 门店地址 */
    private String address;
    private LocalDateTime createtime;
    private LocalDateTime updatetime;
}
