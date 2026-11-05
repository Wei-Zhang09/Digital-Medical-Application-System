package com.qst.medical.mapper;

import com.qst.medical.domain.entity.MedicalPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MedicalPolicyMapper {

    List<MedicalPolicy> selectMedicalPolicy(@Param("title") String title);

    MedicalPolicy selectById(@Param("id") Long id);

    int insertMedicalPolicy(MedicalPolicy policy);

    int updateMedicalPolicy(MedicalPolicy policy);

    int deleteById(@Param("id") Long id);
}
