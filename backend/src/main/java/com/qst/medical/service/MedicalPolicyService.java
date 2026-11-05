package com.qst.medical.service;

import com.qst.medical.common.PageBean;
import com.qst.medical.domain.param.MedicalPolicyParam;

public interface MedicalPolicyService {

    PageBean getMedicalPolicyList(Integer pageNum, Integer pageSize, String title);

    void addMedicalPolicy(MedicalPolicyParam param);

    void updateMedicalPolicy(Long id, MedicalPolicyParam param);

    void deleteMedicalPolicy(Long id);
}
