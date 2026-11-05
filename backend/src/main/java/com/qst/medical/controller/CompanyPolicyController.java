package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.param.CompanyPolicyParam;
import com.qst.medical.service.CompanyPolicyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Min;

@Api(tags = "医药公司政策")
@RestController
@RequestMapping("/api/companyPolicies")
public class CompanyPolicyController {

    @Resource
    private CompanyPolicyService companyPolicyService;

    @ApiOperation("分页查询公司政策")
    @GetMapping("/{pageNum}/{pageSize}")
    public Msg getCompanyPolicyList(@PathVariable @Min(1) Integer pageNum,
                                    @PathVariable @Min(1) Integer pageSize,
                                    @RequestParam(required = false) String title) {
        return Msg.success().data("companyPolicyPageInfo",
                companyPolicyService.getCompanyPolicyList(pageNum, pageSize, title));
    }

    @ApiOperation("新增公司政策（管理员）")
    @PostMapping
    @RolesAllowed({"ROLE_1"})
    public Msg addCompanyPolicy(@RequestBody @Validated CompanyPolicyParam param) {
        companyPolicyService.addCompanyPolicy(param);
        return Msg.success("新增成功");
    }

    @ApiOperation("修改公司政策（管理员）")
    @PutMapping("/{id}")
    @RolesAllowed({"ROLE_1"})
    public Msg updateCompanyPolicy(@PathVariable Long id, @RequestBody @Validated CompanyPolicyParam param) {
        companyPolicyService.updateCompanyPolicy(id, param);
        return Msg.success("修改成功");
    }

    @ApiOperation("删除公司政策（管理员）")
    @DeleteMapping("/{id}")
    @RolesAllowed({"ROLE_1"})
    public Msg deleteCompanyPolicy(@PathVariable Long id) {
        companyPolicyService.deleteCompanyPolicy(id);
        return Msg.success("删除成功");
    }
}
