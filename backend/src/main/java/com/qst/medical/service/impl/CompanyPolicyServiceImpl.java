package com.qst.medical.service.impl;

import com.github.pagehelper.PageHelper;
import com.qst.medical.common.PageBean;
import com.qst.medical.common.PageUtil;
import com.qst.medical.common.SortUtil;
import com.qst.medical.domain.entity.CompanyPolicy;
import com.qst.medical.domain.param.CompanyPolicyParam;
import com.qst.medical.mapper.CompanyMapper;
import com.qst.medical.mapper.CompanyPolicyMapper;
import com.qst.medical.service.CompanyPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class CompanyPolicyServiceImpl implements CompanyPolicyService {

    @Resource
    private CompanyPolicyMapper companyPolicyMapper;
    @Resource
    private CompanyMapper companyMapper;

    @Override
    public PageBean getCompanyPolicyList(Integer pageNum, Integer pageSize, String title, String sort, String order) {
        PageHelper.startPage(pageNum, pageSize, SortUtil.build("companyPolicy", sort, order));
        return PageUtil.toPageBean(companyPolicyMapper.selectCompanyPolicy(title));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCompanyPolicy(CompanyPolicyParam param) {
        checkCompany(param.getCompanyId());
        CompanyPolicy policy = new CompanyPolicy();
        policy.setTitle(param.getTitle().trim());
        policy.setMessage(param.getMessage());
        policy.setCompanyId(param.getCompanyId());
        LocalDateTime now = LocalDateTime.now();
        policy.setCreateTime(now);
        policy.setUpdateTime(now);
        companyPolicyMapper.insertCompanyPolicy(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCompanyPolicy(Long id, CompanyPolicyParam param) {
        CompanyPolicy policy = companyPolicyMapper.selectById(id);
        if (policy == null) {
            throw new IllegalArgumentException("公司政策不存在：" + id);
        }
        checkCompany(param.getCompanyId());
        policy.setTitle(param.getTitle().trim());
        policy.setMessage(param.getMessage());
        policy.setCompanyId(param.getCompanyId());
        policy.setUpdateTime(LocalDateTime.now());
        companyPolicyMapper.updateCompanyPolicy(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCompanyPolicy(Long id) {
        if (companyPolicyMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("公司政策不存在：" + id);
        }
    }

    /** 政策必须挂到已存在的公司上 */
    private void checkCompany(Long companyId) {
        if (companyMapper.selectById(companyId) == null) {
            throw new IllegalArgumentException("医药公司不存在：" + companyId);
        }
    }
}
