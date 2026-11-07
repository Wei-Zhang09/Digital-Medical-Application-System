package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.param.MedicalPolicyParam;
import com.qst.medical.service.MedicalPolicyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Min;

@Api(tags = "医保政策")
@RestController
@RequestMapping("/api/medicalPolicies")
public class MedicalPolicyController {

    @Resource
    private MedicalPolicyService medicalPolicyService;

    @ApiOperation("分页查询医保政策")
    @GetMapping("/{pageNum}/{pageSize}")
    public Msg getMedicalPolicyList(@PathVariable @Min(1) Integer pageNum,
                                    @PathVariable @Min(1) Integer pageSize,
                                    @RequestParam(required = false) String title,
                           @RequestParam(required = false) String sort,
                           @RequestParam(required = false) String order) {
        return Msg.success().data("medicalPolicyPageInfo",
                medicalPolicyService.getMedicalPolicyList(pageNum, pageSize, title, sort, order));
    }

    @ApiOperation("新增医保政策（管理员）")
    @PostMapping
    @RolesAllowed({"ROLE_1"})
    public Msg addMedicalPolicy(@RequestBody @Validated MedicalPolicyParam param) {
        medicalPolicyService.addMedicalPolicy(param);
        return Msg.success("新增成功");
    }

    @ApiOperation("修改医保政策（管理员）")
    @PutMapping("/{id}")
    @RolesAllowed({"ROLE_1"})
    public Msg updateMedicalPolicy(@PathVariable Long id, @RequestBody @Validated MedicalPolicyParam param) {
        medicalPolicyService.updateMedicalPolicy(id, param);
        return Msg.success("修改成功");
    }

    @ApiOperation("删除医保政策（管理员）")
    @DeleteMapping("/{id}")
    @RolesAllowed({"ROLE_1"})
    public Msg deleteMedicalPolicy(@PathVariable Long id) {
        medicalPolicyService.deleteMedicalPolicy(id);
        return Msg.success("删除成功");
    }
}
