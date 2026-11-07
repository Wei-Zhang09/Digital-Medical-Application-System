package com.qst.medical.service.impl;

import com.github.pagehelper.PageHelper;
import com.qst.medical.common.PageBean;
import com.qst.medical.common.PageUtil;
import com.qst.medical.common.SortUtil;
import com.qst.medical.domain.entity.Drug;
import com.qst.medical.domain.param.DrugParam;
import com.qst.medical.mapper.DrugMapper;
import com.qst.medical.mapper.SaleMapper;
import com.qst.medical.service.DrugService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DrugServiceImpl implements DrugService {

    @Resource
    private DrugMapper drugMapper;
    @Resource
    private SaleMapper saleMapper;

    @Override
    public PageBean getDrugList(Integer pageNum, Integer pageSize, String name, String drugInfo, String sort, String order) {
        PageHelper.startPage(pageNum, pageSize, SortUtil.build("drug", sort, order));
        return PageUtil.toPageBean(drugMapper.selectDrug(name, drugInfo));
    }

    @Override
    public Drug getDrugDetail(Long drugId) {
        Drug drug = drugMapper.selectById(drugId);
        if (drug == null) {
            throw new IllegalArgumentException("药品不存在：" + drugId);
        }
        drug.setSaleIds(drugMapper.selectSaleIdsByDrugId(drugId));
        return drug;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDrug(DrugParam param) {
        List<Long> saleIds = checkSales(param.getSaleIds());
        Drug drug = new Drug();
        drug.setDrugName(param.getDrugName().trim());
        drug.setDrugInfo(param.getDrugInfo());
        drug.setDrugEffect(param.getDrugEffect());
        drug.setDrugImg(param.getDrugImg());
        LocalDateTime now = LocalDateTime.now();
        drug.setCreatetime(now);
        drug.setUpdatetime(now);
        drugMapper.insertDrug(drug);
        if (!saleIds.isEmpty()) {
            drugMapper.insertDrugSale(drug.getDrugId(), saleIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDrug(Long drugId, DrugParam param) {
        Drug drug = drugMapper.selectById(drugId);
        if (drug == null) {
            throw new IllegalArgumentException("药品不存在：" + drugId);
        }
        List<Long> saleIds = checkSales(param.getSaleIds());
        drug.setDrugName(param.getDrugName().trim());
        drug.setDrugInfo(param.getDrugInfo());
        drug.setDrugEffect(param.getDrugEffect());
        drug.setDrugImg(param.getDrugImg());
        drug.setUpdatetime(LocalDateTime.now());
        drugMapper.updateDrug(drug);

        // 关联关系整体替换：先删后插，避免残留旧关系
        drugMapper.deleteDrugSaleByDrugId(drugId);
        if (!saleIds.isEmpty()) {
            drugMapper.insertDrugSale(drugId, saleIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDrug(Long drugId) {
        if (drugMapper.selectById(drugId) == null) {
            throw new IllegalArgumentException("药品不存在：" + drugId);
        }
        // 先清关联表，再删主表（drug_sale 没有外键，但顺序上更稳妥）
        drugMapper.deleteDrugSaleByDrugId(drugId);
        drugMapper.deleteById(drugId);
    }

    /** 校验药店存在，返回去重后的 id 列表 */
    private List<Long> checkSales(List<Long> saleIds) {
        if (saleIds == null || saleIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Long> distinct = saleIds.stream().filter(java.util.Objects::nonNull).distinct().collect(java.util.stream.Collectors.toList());
        for (Long saleId : distinct) {
            if (saleMapper.selectById(saleId) == null) {
                throw new IllegalArgumentException("销售地点不存在：" + saleId);
            }
        }
        return distinct;
    }
}
