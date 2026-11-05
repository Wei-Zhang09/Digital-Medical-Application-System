package com.qst.medical.mapper;

import com.qst.medical.domain.entity.City;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CityMapper {

    List<City> selectCity(@Param("cityNumber") Integer cityNumber);

    List<City> selectAllCity();

    City selectById(@Param("cityId") Long cityId);

    int insertCity(City city);

    int deleteById(@Param("cityId") Long cityId);
}
