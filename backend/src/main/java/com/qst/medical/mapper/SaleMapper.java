package com.qst.medical.mapper;

import com.qst.medical.domain.entity.Sale;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SaleMapper {

    List<Sale> selectSale(@Param("name") String name);

    List<Sale> selectAllSale();

    Sale selectById(@Param("saleId") Long saleId);

    int insertSale(Sale sale);

    int updateSale(Sale sale);

    int deleteById(@Param("saleId") Long saleId);
}
