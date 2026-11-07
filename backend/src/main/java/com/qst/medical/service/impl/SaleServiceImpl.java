package com.qst.medical.service.impl;

import com.github.pagehelper.PageHelper;
import com.qst.medical.common.PageBean;
import com.qst.medical.common.PageUtil;
import com.qst.medical.common.SortUtil;
import com.qst.medical.domain.entity.Sale;
import com.qst.medical.domain.param.SaleParam;
import com.qst.medical.mapper.SaleMapper;
import com.qst.medical.service.SaleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaleServiceImpl implements SaleService {

    @Resource
    private SaleMapper saleMapper;

    @Override
    public PageBean getSaleList(Integer pageNum, Integer pageSize, String name, String sort, String order) {
        PageHelper.startPage(pageNum, pageSize, SortUtil.build("sale", sort, order));
        return PageUtil.toPageBean(saleMapper.selectSale(name));
    }

    @Override
    public List<Sale> getAllSale() {
        return saleMapper.selectAllSale();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSale(SaleParam param) {
        Sale sale = new Sale();
        sale.setSaleName(param.getSaleName().trim());
        sale.setSalePhone(param.getSalePhone().trim());
        sale.setLng(param.getLng());
        sale.setLat(param.getLat());
        sale.setAddress(param.getAddress());
        LocalDateTime now = LocalDateTime.now();
        sale.setCreatetime(now);
        sale.setUpdatetime(now);
        saleMapper.insertSale(sale);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSale(Long saleId, SaleParam param) {
        Sale sale = saleMapper.selectById(saleId);
        if (sale == null) {
            throw new IllegalArgumentException("销售地点不存在：" + saleId);
        }
        sale.setSaleName(param.getSaleName().trim());
        sale.setSalePhone(param.getSalePhone().trim());
        sale.setLng(param.getLng());
        sale.setLat(param.getLat());
        sale.setAddress(param.getAddress());
        sale.setUpdatetime(LocalDateTime.now());
        saleMapper.updateSale(sale);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSale(Long saleId) {
        if (saleMapper.deleteById(saleId) == 0) {
            throw new IllegalArgumentException("销售地点不存在：" + saleId);
        }
    }
}
