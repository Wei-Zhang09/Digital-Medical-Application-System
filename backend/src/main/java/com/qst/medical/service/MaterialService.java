package com.qst.medical.service;

import com.qst.medical.common.PageBean;
import com.qst.medical.domain.param.MaterialParam;

/**
 * 必备材料服务。
 */
public interface MaterialService {

    PageBean getMaterialList(Integer pageNum, Integer pageSize, String title);

    void addMaterial(MaterialParam param);

    void updateMaterial(Integer id, MaterialParam param);

    void deleteMaterial(Integer id);
}
