package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.param.CityParam;
import com.qst.medical.service.CityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Min;

/**
 * 城市信息管理：查询对所有角色开放，增删仅管理员。
 */
@Api(tags = "城市信息")
@RestController
@RequestMapping("/api/cities")
public class CityController {

    @Resource
    private CityService cityService;

    @ApiOperation("分页查询城市")
    @GetMapping("/{pageNum}/{pageSize}")
    public Msg getCityList(@PathVariable @Min(1) Integer pageNum,
                           @PathVariable @Min(1) Integer pageSize,
                           @RequestParam(required = false) Integer cityNumber,
                           @RequestParam(required = false) String sort,
                           @RequestParam(required = false) String order) {
        return Msg.success().data("cityPageInfo",
                cityService.getCityList(pageNum, pageSize, cityNumber, sort, order));
    }

    @ApiOperation("全部城市（下拉框用）")
    @GetMapping("/all")
    public Msg getAllCity() {
        return Msg.success().data("cities", cityService.getAllCity());
    }

    @ApiOperation("新增城市（管理员）")
    @PostMapping
    @RolesAllowed({"ROLE_1"})
    public Msg addCity(@RequestBody @Validated CityParam param) {
        cityService.addCity(param);
        return Msg.success("新增成功");
    }

    @ApiOperation("删除城市（管理员）")
    @DeleteMapping("/{cityId}")
    @RolesAllowed({"ROLE_1"})
    public Msg deleteCity(@PathVariable Long cityId) {
        cityService.deleteCity(cityId);
        return Msg.success("删除成功");
    }
}
