package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.param.CompanyParam;
import com.qst.medical.service.CompanyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Min;

@Api(tags = "医药公司")
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Resource
    private CompanyService companyService;

    @ApiOperation("分页查询医药公司")
    @GetMapping("/{pageNum}/{pageSize}")
    public Msg getCompanyList(@PathVariable @Min(1) Integer pageNum,
                              @PathVariable @Min(1) Integer pageSize,
                              @RequestParam(required = false) String name) {
        return Msg.success().data("companyPageInfo",
                companyService.getCompanyList(pageNum, pageSize, name));
    }

    @ApiOperation("全部医药公司（下拉框用）")
    @GetMapping("/all")
    public Msg getAllCompany() {
        return Msg.success().data("companies", companyService.getAllCompany());
    }

    @ApiOperation("新增医药公司（管理员）")
    @PostMapping
    @RolesAllowed({"ROLE_1"})
    public Msg addCompany(@RequestBody @Validated CompanyParam param) {
        companyService.addCompany(param);
        return Msg.success("新增成功");
    }

    @ApiOperation("修改医药公司（管理员）")
    @PutMapping("/{companyId}")
    @RolesAllowed({"ROLE_1"})
    public Msg updateCompany(@PathVariable Long companyId, @RequestBody @Validated CompanyParam param) {
        companyService.updateCompany(companyId, param);
        return Msg.success("修改成功");
    }

    @ApiOperation("删除医药公司（管理员）")
    @DeleteMapping("/{companyId}")
    @RolesAllowed({"ROLE_1"})
    public Msg deleteCompany(@PathVariable Long companyId) {
        companyService.deleteCompany(companyId);
        return Msg.success("删除成功");
    }
}
