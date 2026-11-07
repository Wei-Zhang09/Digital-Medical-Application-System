package com.qst.medical.service;

import com.qst.medical.common.PageBean;
import com.qst.medical.domain.entity.DoctorLevel;
import com.qst.medical.domain.entity.TreatType;
import com.qst.medical.domain.param.DoctorParam;

import java.util.List;

/**
 * 医师服务：医师信息与登录账号同步维护。
 */
public interface DoctorService {

    PageBean getDoctorList(Integer pageNum, Integer pageSize, String name,
                           Integer levelId, Integer typeId, String sort, String order);

    /** 医师级别（职称）下拉框 */
    List<DoctorLevel> getAllLevel();

    /** 诊治类型（科室）下拉框 */
    List<TreatType> getAllTreatType();

    /** 新增医师：同时创建 ROLE_2 登录账号 */
    void addDoctor(DoctorParam param);

    void updateDoctor(Long id, DoctorParam param);

    /** 删除医师：同时删除其账号 */
    void deleteDoctor(Long id);

    /** 重置口令为 123456 */
    void resetPassword(Long id);
}
