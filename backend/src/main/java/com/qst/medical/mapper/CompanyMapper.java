package com.qst.medical.mapper;

import com.qst.medical.domain.entity.Company;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompanyMapper {

    List<Company> selectCompany(@Param("name") String name);

    List<Company> selectAllCompany();

    Company selectById(@Param("companyId") Long companyId);

    int insertCompany(Company company);

    int updateCompany(Company company);

    int deleteById(@Param("companyId") Long companyId);
}
