package com.qst.medical.service;

import com.qst.medical.common.PageBean;
import com.qst.medical.domain.entity.Drug;
import com.qst.medical.domain.param.DrugParam;

/**
 * 药品服务。
 */
public interface DrugService {

    PageBean getDrugList(Integer pageNum, Integer pageSize, String name);

    /** 详情（含关联药店 id），编辑弹窗用 */
    Drug getDrugDetail(Long drugId);

    void addDrug(DrugParam param);

    void updateDrug(Long drugId, DrugParam param);

    void deleteDrug(Long drugId);
}
