package com.qst.medical.mapper;

import com.qst.medical.domain.model.ChartItemModel;
import com.qst.medical.domain.model.DashboardModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardMapper {

    long countDoctor();

    long countCompany();

    long countDrug();

    long countSale();

    /** 按医师职称分组统计 */
    List<ChartItemModel> countByLevel();

    /** 按诊治类型（科室）分组统计 */
    List<ChartItemModel> countByType();

    /** 最新资讯：三种内容表按时间合并取前 N 条 */
    List<DashboardModel.NewsItemModel> selectLatestNews(@Param("limit") int limit);
}
