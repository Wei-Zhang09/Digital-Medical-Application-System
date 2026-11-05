package com.qst.medical.service.impl;

import com.github.pagehelper.PageHelper;
import com.qst.medical.common.PageBean;
import com.qst.medical.common.PageUtil;
import com.qst.medical.domain.entity.Material;
import com.qst.medical.domain.param.MaterialParam;
import com.qst.medical.mapper.MaterialMapper;
import com.qst.medical.service.MaterialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class MaterialServiceImpl implements MaterialService {

    @Resource
    private MaterialMapper materialMapper;

    @Override
    public PageBean getMaterialList(Integer pageNum, Integer pageSize, String title) {
        PageHelper.startPage(pageNum, pageSize);
        return PageUtil.toPageBean(materialMapper.selectMaterial(title));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMaterial(MaterialParam param) {
        Material material = new Material();
        material.setTitle(param.getTitle().trim());
        material.setMessage(param.getMessage());
        LocalDateTime now = LocalDateTime.now();
        material.setCreateTime(now);
        material.setUpdateTime(now);
        materialMapper.insertMaterial(material);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMaterial(Integer id, MaterialParam param) {
        Material material = materialMapper.selectById(id);
        if (material == null) {
            throw new IllegalArgumentException("必备材料不存在：" + id);
        }
        material.setTitle(param.getTitle().trim());
        material.setMessage(param.getMessage());
        material.setUpdateTime(LocalDateTime.now());
        materialMapper.updateMaterial(material);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaterial(Integer id) {
        if (materialMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("必备材料不存在：" + id);
        }
    }
}
