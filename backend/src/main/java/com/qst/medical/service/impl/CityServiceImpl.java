package com.qst.medical.service.impl;

import com.github.pagehelper.PageHelper;
import com.qst.medical.common.PageBean;
import com.qst.medical.common.PageUtil;
import com.qst.medical.common.SortUtil;
import com.qst.medical.domain.entity.City;
import com.qst.medical.domain.param.CityParam;
import com.qst.medical.mapper.CityMapper;
import com.qst.medical.service.CityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CityServiceImpl implements CityService {

    @Resource
    private CityMapper cityMapper;

    @Override
    public PageBean getCityList(Integer pageNum, Integer pageSize, Integer cityNumber, String sort, String order) {
        PageHelper.startPage(pageNum, pageSize, SortUtil.build("city", sort, order));
        return PageUtil.toPageBean(cityMapper.selectCity(cityNumber));
    }

    @Override
    public List<City> getAllCity() {
        return cityMapper.selectAllCity();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCity(CityParam param) {
        City city = new City();
        city.setCityNumber(param.getCityNumber());
        LocalDateTime now = LocalDateTime.now();
        city.setCreatetime(now);
        city.setUpdatetime(now);
        cityMapper.insertCity(city);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCity(Long cityId) {
        if (cityMapper.deleteById(cityId) == 0) {
            throw new IllegalArgumentException("城市不存在：" + cityId);
        }
    }
}
