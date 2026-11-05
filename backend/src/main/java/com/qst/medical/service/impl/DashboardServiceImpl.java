package com.qst.medical.service.impl;

import com.qst.medical.domain.model.DashboardModel;
import com.qst.medical.mapper.DashboardMapper;
import com.qst.medical.service.DashboardService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DashboardServiceImpl implements DashboardService {

    /** 首页资讯展示条数 */
    private static final int NEWS_LIMIT = 5;

    @Resource
    private DashboardMapper dashboardMapper;

    @Override
    public DashboardModel getDashboard() {
        DashboardModel model = new DashboardModel();
        model.setDoctorCount(dashboardMapper.countDoctor());
        model.setCompanyCount(dashboardMapper.countCompany());
        model.setDrugCount(dashboardMapper.countDrug());
        model.setSaleCount(dashboardMapper.countSale());
        model.setLevelChart(dashboardMapper.countByLevel());
        model.setTypeChart(dashboardMapper.countByType());
        model.setNews(dashboardMapper.selectLatestNews(NEWS_LIMIT));
        return model;
    }
}
