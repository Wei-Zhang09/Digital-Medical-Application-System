package com.qst.medical.mapper;

import com.qst.medical.domain.entity.Drug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DrugMapper {

    /** 按药名模糊查询，联表带出关联药店名称 */
    List<Drug> selectDrug(@Param("name") String name, @Param("drugInfo") String drugInfo);

    Drug selectById(@Param("drugId") Long drugId);

    /** 某药品关联的药店 id */
    List<Long> selectSaleIdsByDrugId(@Param("drugId") Long drugId);

    int insertDrug(Drug drug);

    int updateDrug(Drug drug);

    int deleteById(@Param("drugId") Long drugId);

    int insertDrugSale(@Param("drugId") Long drugId, @Param("saleIds") List<Long> saleIds);

    int deleteDrugSaleByDrugId(@Param("drugId") Long drugId);
}
