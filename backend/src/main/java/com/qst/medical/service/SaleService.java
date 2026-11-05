package com.qst.medical.service;

import com.qst.medical.common.PageBean;
import com.qst.medical.domain.entity.Sale;
import com.qst.medical.domain.param.SaleParam;

import java.util.List;

public interface SaleService {

    PageBean getSaleList(Integer pageNum, Integer pageSize, String name);

    List<Sale> getAllSale();

    void addSale(SaleParam param);

    void updateSale(Long saleId, SaleParam param);

    void deleteSale(Long saleId);
}
