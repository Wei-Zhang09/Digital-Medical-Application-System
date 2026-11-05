package com.qst.medical.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 图表数据项：{name, value}，ECharts 的柱状图/饼图都吃这个结构。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartItemModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Long value;
}
