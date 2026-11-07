package com.qst.medical.service.impl;

import com.github.pagehelper.PageHelper;
import com.qst.medical.common.PageBean;
import com.qst.medical.common.PageUtil;
import com.qst.medical.common.SortUtil;
import com.qst.medical.domain.entity.MedicalPolicy;
import com.qst.medical.domain.param.MedicalPolicyParam;
import com.qst.medical.mapper.CityMapper;
import com.qst.medical.mapper.MedicalPolicyMapper;
import com.qst.medical.service.MedicalPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MedicalPolicyServiceImpl implements MedicalPolicyService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private MedicalPolicyMapper medicalPolicyMapper;
    @Resource
    private CityMapper cityMapper;

    @Override
    public PageBean getMedicalPolicyList(Integer pageNum, Integer pageSize, String title, String sort, String order) {
        PageHelper.startPage(pageNum, pageSize, SortUtil.build("medicalPolicy", sort, order));
        return PageUtil.toPageBean(medicalPolicyMapper.selectMedicalPolicy(title));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMedicalPolicy(MedicalPolicyParam param) {
        checkCity(param.getCityId());
        MedicalPolicy policy = new MedicalPolicy();
        policy.setTitle(param.getTitle().trim());
        policy.setMessage(param.getMessage());
        policy.setCityId(param.getCityId());
        String now = LocalDateTime.now().format(FMT);
        policy.setCreateTime(now);
        policy.setUpdateTime(now);
        medicalPolicyMapper.insertMedicalPolicy(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMedicalPolicy(Long id, MedicalPolicyParam param) {
        MedicalPolicy policy = medicalPolicyMapper.selectById(id);
        if (policy == null) {
            throw new IllegalArgumentException("医保政策不存在：" + id);
        }
        checkCity(param.getCityId());
        policy.setTitle(param.getTitle().trim());
        policy.setMessage(param.getMessage());
        policy.setCityId(param.getCityId());
        policy.setUpdateTime(LocalDateTime.now().format(FMT));
        medicalPolicyMapper.updateMedicalPolicy(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMedicalPolicy(Long id) {
        if (medicalPolicyMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("医保政策不存在：" + id);
        }
    }

    /** 医保政策必须挂在已存在的城市上 */
    private void checkCity(Long cityId) {
        if (cityMapper.selectById(cityId) == null) {
            throw new IllegalArgumentException("城市不存在：" + cityId);
        }
    }
}
