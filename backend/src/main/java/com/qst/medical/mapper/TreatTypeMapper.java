package com.qst.medical.mapper;

import com.qst.medical.domain.entity.TreatType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TreatTypeMapper {

    List<TreatType> selectAll();
}
