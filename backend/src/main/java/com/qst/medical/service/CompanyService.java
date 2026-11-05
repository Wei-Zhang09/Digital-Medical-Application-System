package com.qst.medical.service;

import com.qst.medical.common.PageBean;
import com.qst.medical.domain.entity.Company;
import com.qst.medical.domain.param.CompanyParam;

import java.util.List;

public interface CompanyService {

    PageBean getCompanyList(Integer pageNum, Integer pageSize, String name);

    List<Company> getAllCompany();

    void addCompany(CompanyParam param);

    void updateCompany(Long companyId, CompanyParam param);

    void deleteCompany(Long companyId);
}
