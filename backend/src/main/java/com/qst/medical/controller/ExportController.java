package com.qst.medical.controller;

import com.qst.medical.common.ExcelExporter;
import com.qst.medical.common.PageBean;
import com.qst.medical.domain.entity.Account;
import com.qst.medical.domain.entity.City;
import com.qst.medical.domain.entity.Company;
import com.qst.medical.domain.entity.CompanyPolicy;
import com.qst.medical.domain.entity.Doctor;
import com.qst.medical.domain.entity.Drug;
import com.qst.medical.domain.entity.Material;
import com.qst.medical.domain.entity.MedicalPolicy;
import com.qst.medical.domain.entity.Sale;
import com.qst.medical.service.AccountService;
import com.qst.medical.service.CityService;
import com.qst.medical.service.CompanyPolicyService;
import com.qst.medical.service.CompanyService;
import com.qst.medical.service.DoctorService;
import com.qst.medical.service.DrugService;
import com.qst.medical.service.MaterialService;
import com.qst.medical.service.MedicalPolicyService;
import com.qst.medical.service.SaleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出接口：把「当前筛选条件下的全部数据」导成 .xlsx。
 *
 * <p>复用各模块自己的列表服务（pageSize 给一个上限），
 * 保证导出的内容与页面上看到的完全一致，不另写一套查询。</p>
 */
@Api(tags = "数据导出")
@RestController
@RequestMapping("/api/export")
public class ExportController {

    /** 单次导出上限，避免一次拉爆内存 */
    private static final int MAX_ROWS = 10000;

    @Resource
    private DoctorService doctorService;
    @Resource
    private DrugService drugService;
    @Resource
    private CompanyService companyService;
    @Resource
    private SaleService saleService;
    @Resource
    private CityService cityService;
    @Resource
    private MedicalPolicyService medicalPolicyService;
    @Resource
    private CompanyPolicyService companyPolicyService;
    @Resource
    private MaterialService materialService;
    @Resource
    private AccountService accountService;

    @ApiOperation("导出 Excel（模块：doctor/drug/company/sale/city/medicalPolicy/companyPolicy/material/account）")
    @GetMapping("/{module}")
    @RolesAllowed({"ROLE_1", "ROLE_2"})
    public void export(@PathVariable String module,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String title,
                       @RequestParam(required = false) Integer cityNumber,
                       @RequestParam(required = false) Integer levelId,
                       @RequestParam(required = false) Integer typeId,
                       @RequestParam(required = false) String drugInfo,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false) String order,
                       HttpServletResponse response) throws IOException {

        String kw = trimToNull(keyword);
        switch (module) {
            case "doctor":
                exportDoctors(response, name, levelId, typeId, sort, order);
                break;
            case "drug":
                exportDrugs(response, name, drugInfo, sort, order);
                break;
            case "company":
                exportCompanies(response, name, sort, order);
                break;
            case "sale":
                exportSales(response, name, sort, order);
                break;
            case "city":
                exportCities(response, cityNumber, sort, order);
                break;
            case "medicalPolicy":
                exportMedicalPolicies(response, title, sort, order);
                break;
            case "companyPolicy":
                exportCompanyPolicies(response, title, sort, order);
                break;
            case "material":
                exportMaterials(response, title, sort, order);
                break;
            case "account":
                exportAccounts(response, kw, sort, order);
                break;
            default:
                throw new IllegalArgumentException("不支持的导出模块：" + module);
        }
    }

    // ==================== 各模块 ====================

    private void exportDoctors(HttpServletResponse resp, String name, Integer levelId, Integer typeId,
                               String sort, String order) throws IOException {
        PageBean page = doctorService.getDoctorList(1, MAX_ROWS, name, levelId, typeId, sort, order);
        List<String[]> rows = new ArrayList<>();
        for (Object o : page.getList()) {
            Doctor d = (Doctor) o;
            rows.add(new String[]{
                    ExcelExporter.str(d.getId()), ExcelExporter.str(d.getName()), ExcelExporter.str(d.getAge()),
                    ExcelExporter.str(d.getSex()), ExcelExporter.str(d.getLevelName()), ExcelExporter.str(d.getTypeName()),
                    ExcelExporter.str(d.getPhone()), ExcelExporter.str(d.getHospital()), ExcelExporter.str(d.getUname())
            });
        }
        ExcelExporter.write(resp, "医师信息", "医师信息",
                new String[]{"ID", "姓名", "年龄", "性别", "职称", "科室", "联系电话", "所属医院", "登录名"}, rows);
    }

    private void exportDrugs(HttpServletResponse resp, String name, String drugInfo,
                             String sort, String order) throws IOException {
        PageBean page = drugService.getDrugList(1, MAX_ROWS, name, drugInfo, sort, order);
        List<String[]> rows = new ArrayList<>();
        for (Object o : page.getList()) {
            Drug d = (Drug) o;
            rows.add(new String[]{
                    ExcelExporter.str(d.getDrugName()), ExcelExporter.str(d.getDrugInfo()),
                    ExcelExporter.str(d.getDrugEffect()), ExcelExporter.str(d.getSaleNames()),
                    ExcelExporter.str(d.getUpdatetime())
            });
        }
        ExcelExporter.write(resp, "药品信息", "药品信息",
                new String[]{"药名", "药品成分", "药品功能", "销售药店", "更新时间"}, rows);
    }

    private void exportCompanies(HttpServletResponse resp, String name, String sort, String order) throws IOException {
        PageBean page = companyService.getCompanyList(1, MAX_ROWS, name, sort, order);
        List<String[]> rows = new ArrayList<>();
        for (Object o : page.getList()) {
            Company c = (Company) o;
            rows.add(new String[]{
                    ExcelExporter.str(c.getCompanyId()), ExcelExporter.str(c.getCompanyName()),
                    ExcelExporter.str(c.getCompanyPhone()), ExcelExporter.str(c.getCreatetime())
            });
        }
        ExcelExporter.write(resp, "医药公司", "医药公司",
                new String[]{"公司ID", "公司名称", "联系电话", "创建时间"}, rows);
    }

    private void exportSales(HttpServletResponse resp, String name, String sort, String order) throws IOException {
        PageBean page = saleService.getSaleList(1, MAX_ROWS, name, sort, order);
        List<String[]> rows = new ArrayList<>();
        for (Object o : page.getList()) {
            Sale s = (Sale) o;
            rows.add(new String[]{
                    ExcelExporter.str(s.getSaleId()), ExcelExporter.str(s.getSaleName()),
                    ExcelExporter.str(s.getSalePhone()), ExcelExporter.str(s.getAddress()),
                    ExcelExporter.str(s.getLng()), ExcelExporter.str(s.getLat()),
                    ExcelExporter.str(s.getCreatetime())
            });
        }
        ExcelExporter.write(resp, "销售地点", "销售地点",
                new String[]{"ID", "药店名称", "联系电话", "地址", "经度", "纬度", "创建时间"}, rows);
    }

    private void exportCities(HttpServletResponse resp, Integer cityNumber, String sort, String order) throws IOException {
        PageBean page = cityService.getCityList(1, MAX_ROWS, cityNumber, sort, order);
        List<String[]> rows = new ArrayList<>();
        for (Object o : page.getList()) {
            City c = (City) o;
            rows.add(new String[]{
                    ExcelExporter.str(c.getCityId()), ExcelExporter.str(c.getCityNumber()),
                    ExcelExporter.str(c.getCityName()), ExcelExporter.str(c.getCreatetime())
            });
        }
        ExcelExporter.write(resp, "城市信息", "城市信息",
                new String[]{"城市ID", "行政区划代码", "城市名称", "创建时间"}, rows);
    }

    private void exportMedicalPolicies(HttpServletResponse resp, String title, String sort, String order) throws IOException {
        PageBean page = medicalPolicyService.getMedicalPolicyList(1, MAX_ROWS, title, sort, order);
        Map<Long, String> cityNames = cityNameMap();
        List<String[]> rows = new ArrayList<>();
        for (Object o : page.getList()) {
            MedicalPolicy p = (MedicalPolicy) o;
            rows.add(new String[]{
                    ExcelExporter.str(p.getId()), ExcelExporter.str(p.getTitle()),
                    cityNames.getOrDefault(p.getCityId(), ExcelExporter.str(p.getCityId())),
                    ExcelExporter.str(p.getMessage()), ExcelExporter.str(p.getUpdateTime())
            });
        }
        ExcelExporter.write(resp, "医保政策", "医保政策",
                new String[]{"编号", "政策标题", "生效城市", "政策内容", "更新时间"}, rows);
    }

    private void exportCompanyPolicies(HttpServletResponse resp, String title, String sort, String order) throws IOException {
        PageBean page = companyPolicyService.getCompanyPolicyList(1, MAX_ROWS, title, sort, order);
        Map<Long, String> companyNames = companyNameMap();
        List<String[]> rows = new ArrayList<>();
        for (Object o : page.getList()) {
            CompanyPolicy p = (CompanyPolicy) o;
            rows.add(new String[]{
                    ExcelExporter.str(p.getId()), ExcelExporter.str(p.getTitle()),
                    companyNames.getOrDefault(p.getCompanyId(), ExcelExporter.str(p.getCompanyId())),
                    ExcelExporter.str(p.getMessage()), ExcelExporter.str(p.getUpdateTime())
            });
        }
        ExcelExporter.write(resp, "公司政策", "公司政策",
                new String[]{"编号", "政策标题", "所属医药公司", "政策内容", "更新时间"}, rows);
    }

    private void exportMaterials(HttpServletResponse resp, String title, String sort, String order) throws IOException {
        PageBean page = materialService.getMaterialList(1, MAX_ROWS, title, sort, order);
        List<String[]> rows = new ArrayList<>();
        for (Object o : page.getList()) {
            Material m = (Material) o;
            rows.add(new String[]{
                    ExcelExporter.str(m.getId()), ExcelExporter.str(m.getTitle()),
                    ExcelExporter.str(m.getMessage()), ExcelExporter.str(m.getUpdateTime())
            });
        }
        ExcelExporter.write(resp, "必备材料", "必备材料",
                new String[]{"编号", "标题", "内容", "更新时间"}, rows);
    }

    private void exportAccounts(HttpServletResponse resp, String keyword, String sort, String order) throws IOException {
        PageBean page = accountService.getAccountList(1, MAX_ROWS, keyword, sort, order);
        List<String[]> rows = new ArrayList<>();
        for (Object o : page.getList()) {
            Account a = (Account) o;
            rows.add(new String[]{
                    ExcelExporter.str(a.getUname()), ExcelExporter.str(a.getRealname()),
                    ExcelExporter.str(a.getPhonenumber()),
                    "ROLE_1".equals(a.getUtype()) ? "管理员" : "医师",
                    ExcelExporter.str(a.getCreatetime())
            });
        }
        ExcelExporter.write(resp, "账号列表", "账号列表",
                new String[]{"登录名", "真实姓名", "手机号", "角色", "创建时间"}, rows);
    }

    // ==================== 工具 ====================

    private Map<Long, String> cityNameMap() {
        Map<Long, String> map = new HashMap<>();
        for (City c : cityService.getAllCity()) {
            map.put(c.getCityId(), c.getCityName());
        }
        return map;
    }

    private Map<Long, String> companyNameMap() {
        Map<Long, String> map = new HashMap<>();
        for (Company c : companyService.getAllCompany()) {
            map.put(c.getCompanyId(), c.getCompanyName());
        }
        return map;
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
