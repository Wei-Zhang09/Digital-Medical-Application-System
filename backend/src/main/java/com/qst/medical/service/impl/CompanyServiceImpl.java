package com.qst.medical.service.impl;

import com.github.pagehelper.PageHelper;
import com.qst.medical.common.PageBean;
import com.qst.medical.common.PageUtil;
import com.qst.medical.domain.entity.Company;
import com.qst.medical.domain.param.CompanyParam;
import com.qst.medical.mapper.CompanyMapper;
import com.qst.medical.service.CompanyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Resource
    private CompanyMapper companyMapper;

    @Override
    public PageBean getCompanyList(Integer pageNum, Integer pageSize, String name) {
        PageHelper.startPage(pageNum, pageSize);
        return PageUtil.toPageBean(companyMapper.selectCompany(name));
    }

    @Override
    public List<Company> getAllCompany() {
        return companyMapper.selectAllCompany();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCompany(CompanyParam param) {
        Company company = new Company();
        company.setCompanyName(param.getCompanyName().trim());
        company.setCompanyPhone(param.getCompanyPhone().trim());
        LocalDateTime now = LocalDateTime.now();
        company.setCreatetime(now);
        company.setUpdatetime(now);
        companyMapper.insertCompany(company);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCompany(Long companyId, CompanyParam param) {
        Company company = companyMapper.selectById(companyId);
        if (company == null) {
            throw new IllegalArgumentException("医药公司不存在：" + companyId);
        }
        company.setCompanyName(param.getCompanyName().trim());
        company.setCompanyPhone(param.getCompanyPhone().trim());
        company.setUpdatetime(LocalDateTime.now());
        companyMapper.updateCompany(company);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCompany(Long companyId) {
        if (companyMapper.deleteById(companyId) == 0) {
            throw new IllegalArgumentException("医药公司不存在：" + companyId);
        }
    }
}
