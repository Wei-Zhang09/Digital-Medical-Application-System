package com.qst.medical.service;

import com.qst.medical.common.PageBean;
import com.qst.medical.domain.param.CompanyPolicyParam;

public interface CompanyPolicyService {

    PageBean getCompanyPolicyList(Integer pageNum, Integer pageSize, String title);

    void addCompanyPolicy(CompanyPolicyParam param);

    void updateCompanyPolicy(Long id, CompanyPolicyParam param);

    void deleteCompanyPolicy(Long id);
}
