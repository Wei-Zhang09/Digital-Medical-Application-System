package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.param.DoctorParam;
import com.qst.medical.service.DoctorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Min;

/**
 * 医生信息管理。
 *
 * <p>查询对管理员与医师都开放（医师只能看，改删被 {@link RolesAllowed} 挡住），
 * 增删改与重置口令仅管理员。</p>
 */
@Api(tags = "医生信息")
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Resource
    private DoctorService doctorService;

    @ApiOperation("分页查询医师")
    @GetMapping("/{pageNum}/{pageSize}")
    public Msg getDoctorList(@PathVariable @Min(1) Integer pageNum,
                             @PathVariable @Min(1) Integer pageSize,
                             @RequestParam(required = false) String name,
                           @RequestParam(required = false) Integer levelId,
                           @RequestParam(required = false) Integer typeId,
                           @RequestParam(required = false) String sort,
                           @RequestParam(required = false) String order) {
        return Msg.success().data("doctorPageInfo",
                doctorService.getDoctorList(pageNum, pageSize, name, levelId, typeId, sort, order));
    }

    @ApiOperation("医师级别下拉框")
    @GetMapping("/levels")
    public Msg getLevels() {
        return Msg.success().data("levels", doctorService.getAllLevel());
    }

    @ApiOperation("诊治类型下拉框")
    @GetMapping("/types")
    public Msg getTypes() {
        return Msg.success().data("types", doctorService.getAllTreatType());
    }

    @ApiOperation("新增医师（管理员，同时创建账号）")
    @PostMapping
    @RolesAllowed({"ROLE_1"})
    public Msg addDoctor(@RequestBody @Validated DoctorParam param) {
        doctorService.addDoctor(param);
        return Msg.success("新增成功，初始口令 123456");
    }

    @ApiOperation("修改医师（管理员）")
    @PutMapping("/{id}")
    @RolesAllowed({"ROLE_1"})
    public Msg updateDoctor(@PathVariable Long id, @RequestBody @Validated DoctorParam param) {
        doctorService.updateDoctor(id, param);
        return Msg.success("修改成功");
    }

    @ApiOperation("删除医师（管理员，同时删除账号）")
    @DeleteMapping("/{id}")
    @RolesAllowed({"ROLE_1"})
    public Msg deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return Msg.success("删除成功");
    }

    @ApiOperation("重置医师口令为 123456（管理员）")
    @PutMapping("/pwd/{id}")
    @RolesAllowed({"ROLE_1"})
    public Msg resetPassword(@PathVariable Long id) {
        doctorService.resetPassword(id);
        return Msg.success("口令已重置为 123456");
    }
}
