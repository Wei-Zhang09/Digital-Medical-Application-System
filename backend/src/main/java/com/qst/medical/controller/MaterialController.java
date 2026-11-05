package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.param.MaterialParam;
import com.qst.medical.service.MaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.Min;

/**
 * 必备材料管理：所有角色可查，仅管理员可增删改。
 */
@Api(tags = "必备材料")
@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    @Resource
    private MaterialService materialService;

    @ApiOperation("分页查询必备材料")
    @GetMapping("/{pageNum}/{pageSize}")
    public Msg getMaterialList(@PathVariable @Min(1) Integer pageNum,
                               @PathVariable @Min(1) Integer pageSize,
                               @RequestParam(required = false) String title) {
        return Msg.success().data("materialPageInfo",
                materialService.getMaterialList(pageNum, pageSize, title));
    }

    @ApiOperation("新增必备材料（管理员）")
    @PostMapping
    @javax.annotation.security.RolesAllowed({"ROLE_1"})
    public Msg addMaterial(@RequestBody @Validated MaterialParam param) {
        materialService.addMaterial(param);
        return Msg.success("新增成功");
    }

    @ApiOperation("修改必备材料（管理员）")
    @PutMapping("/{id}")
    @javax.annotation.security.RolesAllowed({"ROLE_1"})
    public Msg updateMaterial(@PathVariable Integer id, @RequestBody @Validated MaterialParam param) {
        materialService.updateMaterial(id, param);
        return Msg.success("修改成功");
    }

    @ApiOperation("删除必备材料（管理员）")
    @DeleteMapping("/{id}")
    @javax.annotation.security.RolesAllowed({"ROLE_1"})
    public Msg deleteMaterial(@PathVariable Integer id) {
        materialService.deleteMaterial(id);
        return Msg.success("删除成功");
    }
}
