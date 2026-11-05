package com.qst.medical.domain.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 首页数据面板：四个统计数字 + 两张图 + 最新资讯。
 */
@Data
public class DashboardModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 医师数量 */
    private Long doctorCount;
    /** 医药公司数量 */
    private Long companyCount;
    /** 药品数量 */
    private Long drugCount;
    /** 药店（销售地点）数量 */
    private Long saleCount;

    /** 医师职称分布（柱状图） */
    private List<ChartItemModel> levelChart;
    /** 科室分布（饼图） */
    private List<ChartItemModel> typeChart;

    /** 最新资讯（取最新发布的政策标题） */
    private List<NewsItemModel> news;

    /**
     * 资讯条目：标题 + 时间 + 来源类型
     */
    @Data
    public static class NewsItemModel implements Serializable {
        private static final long serialVersionUID = 1L;
        private String title;
        private String time;
        /** 来源：医保政策 / 公司政策 / 必备材料 */
        private String source;
    }
}
