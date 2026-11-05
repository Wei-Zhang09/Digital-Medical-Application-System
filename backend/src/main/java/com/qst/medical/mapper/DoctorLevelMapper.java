package com.qst.medical.mapper;

import com.qst.medical.domain.entity.DoctorLevel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DoctorLevelMapper {

    List<DoctorLevel> selectAll();
}
