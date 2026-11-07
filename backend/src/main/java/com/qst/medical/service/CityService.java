package com.qst.medical.service;

import com.qst.medical.common.PageBean;
import com.qst.medical.domain.entity.City;
import com.qst.medical.domain.param.CityParam;

import java.util.List;

/**
 * 城市服务。城市只做新增、查询、删除。
 */
public interface CityService {

    PageBean getCityList(Integer pageNum, Integer pageSize, Integer cityNumber, String sort, String order);

    /** 全部城市，供医保政策下拉框使用 */
    List<City> getAllCity();

    void addCity(CityParam param);

    void deleteCity(Long cityId);
}
