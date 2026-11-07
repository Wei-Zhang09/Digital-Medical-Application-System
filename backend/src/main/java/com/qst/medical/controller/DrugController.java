package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.param.DrugParam;
import com.qst.medical.service.DrugService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Min;

/**
 * 药品信息管理：查与增对管理员/医师开放，改删仅管理员（官方任务 21 的接口表）。
 */
@Api(tags = "药品信息")
@RestController
@RequestMapping("/api/drugs")
public class DrugController {

    @Resource
    private DrugService drugService;

    @ApiOperation("分页查询药品")
    @GetMapping("/{pageNum}/{pageSize}")
    public Msg getDrugList(@PathVariable @Min(1) Integer pageNum,
                           @PathVariable @Min(1) Integer pageSize,
                           @RequestParam(required = false) String name,
                           @RequestParam(required = false) String drugInfo,
                           @RequestParam(required = false) String sort,
                           @RequestParam(required = false) String order) {
        return Msg.success().data("drugPageInfo",
                drugService.getDrugList(pageNum, pageSize, name, drugInfo, sort, order));
    }

    @ApiOperation("药品详情（含关联药店）")
    @GetMapping("/{drugId}")
    public Msg getDrugDetail(@PathVariable Long drugId) {
        return Msg.success().data("drug", drugService.getDrugDetail(drugId));
    }

    @ApiOperation("新增药品（管理员/医师）")
    @PostMapping
    @RolesAllowed({"ROLE_1", "ROLE_2"})
    public Msg addDrug(@RequestBody @Validated DrugParam param) {
        drugService.addDrug(param);
        return Msg.success("新增成功");
    }

    @ApiOperation("修改药品（管理员）")
    @PutMapping("/{drugId}")
    @RolesAllowed({"ROLE_1"})
    public Msg updateDrug(@PathVariable Long drugId, @RequestBody @Validated DrugParam param) {
        drugService.updateDrug(drugId, param);
        return Msg.success("修改成功");
    }

    @ApiOperation("删除药品（管理员，同时清理关联药店）")
    @DeleteMapping("/{drugId}")
    @RolesAllowed({"ROLE_1"})
    public Msg deleteDrug(@PathVariable Long drugId) {
        drugService.deleteDrug(drugId);
        return Msg.success("删除成功");
    }
}
