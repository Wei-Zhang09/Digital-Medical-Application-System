package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 城市实体，对应 digital_medical_application_system.city。
 */
@Data
public class City implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long cityId;
    /** 城市编号（业务编号，与主键区分） */
    private Integer cityNumber;
    private LocalDateTime createtime;
    private LocalDateTime updatetime;

    /**
     * 城市名称（展示用，不入库）。
     *
     * <p>city 表本身只有编号，没有名字；这里用 city_number 关联字典表 sysregion 的 id
     * （行政区划代码，Level=2 即地级市），取 MerName 的最后一段，如「唐山市」。</p>
     */
    private String cityName;
}
