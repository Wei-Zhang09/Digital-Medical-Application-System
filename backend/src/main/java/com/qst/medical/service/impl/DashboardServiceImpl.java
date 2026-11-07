package com.qst.medical.service.impl;

import com.qst.medical.domain.entity.CompanyPolicy;
import com.qst.medical.domain.entity.Material;
import com.qst.medical.domain.entity.MedicalPolicy;
import com.qst.medical.domain.model.DashboardModel;
import com.qst.medical.mapper.CompanyPolicyMapper;
import com.qst.medical.mapper.DashboardMapper;
import com.qst.medical.mapper.MaterialMapper;
import com.qst.medical.mapper.MedicalPolicyMapper;
import com.qst.medical.service.DashboardService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    /** 首页资讯展示条数 */
    private static final int NEWS_LIMIT = 5;

    @Resource
    private DashboardMapper dashboardMapper;

    @Resource
    private MedicalPolicyMapper medicalPolicyMapper;

    @Resource
    private CompanyPolicyMapper companyPolicyMapper;

    @Resource
    private MaterialMapper materialMapper;

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

    @Override
    public Map<String, Object> getNewsDetail(String source, Long id) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", id);

        if ("medical".equals(source)) {
            MedicalPolicy policy = medicalPolicyMapper.selectById(id);
            if (policy == null) {
                throw new IllegalArgumentException("医保政策不存在");
            }
            detail.put("source", "医保政策");
            detail.put("modulePath", "/manage/medical/policy");
            detail.put("title", policy.getTitle());
            detail.put("content", policy.getMessage());
            detail.put("time", policy.getCreateTime());
        } else if ("company".equals(source)) {
            CompanyPolicy policy = companyPolicyMapper.selectById(id);
            if (policy == null) {
                throw new IllegalArgumentException("公司政策不存在");
            }
            detail.put("source", "公司政策");
            detail.put("modulePath", "/manage/company/policy");
            detail.put("title", policy.getTitle());
            detail.put("content", policy.getMessage());
            detail.put("time", policy.getCreateTime());
        } else if ("material".equals(source)) {
            Material material = materialMapper.selectById(id.intValue());
            if (material == null) {
                throw new IllegalArgumentException("必备材料不存在");
            }
            detail.put("source", "必备材料");
            detail.put("modulePath", "/manage/material");
            detail.put("title", material.getTitle());
            detail.put("content", material.getMessage());
            detail.put("time", material.getCreateTime());
        } else {
            throw new IllegalArgumentException("未知的资讯来源：" + source);
        }
        return detail;
    }
}
