package com.qst.medical.mapper;

import com.qst.medical.domain.entity.CompanyPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompanyPolicyMapper {

    List<CompanyPolicy> selectCompanyPolicy(@Param("title") String title);

    CompanyPolicy selectById(@Param("id") Long id);

    int insertCompanyPolicy(CompanyPolicy policy);

    int updateCompanyPolicy(CompanyPolicy policy);

    int deleteById(@Param("id") Long id);
}
