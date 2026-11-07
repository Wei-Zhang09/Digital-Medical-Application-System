package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.domain.param.SaleParam;
import com.qst.medical.service.SaleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Min;

@Api(tags = "销售地点")
@RestController
@RequestMapping("/api/sales")
public class SaleController {

    @Resource
    private SaleService saleService;

    @ApiOperation("分页查询销售地点")
    @GetMapping("/{pageNum}/{pageSize}")
    public Msg getSaleList(@PathVariable @Min(1) Integer pageNum,
                           @PathVariable @Min(1) Integer pageSize,
                           @RequestParam(required = false) String name,
                           @RequestParam(required = false) String sort,
                           @RequestParam(required = false) String order) {
        return Msg.success().data("salePageInfo",
                saleService.getSaleList(pageNum, pageSize, name, sort, order));
    }

    @ApiOperation("全部销售地点（下拉框用）")
    @GetMapping("/all")
    public Msg getAllSale() {
        return Msg.success().data("sales", saleService.getAllSale());
    }

    @ApiOperation("新增销售地点（管理员）")
    @PostMapping
    @RolesAllowed({"ROLE_1"})
    public Msg addSale(@RequestBody @Validated SaleParam param) {
        saleService.addSale(param);
        return Msg.success("新增成功");
    }

    @ApiOperation("修改销售地点（管理员）")
    @PutMapping("/{saleId}")
    @RolesAllowed({"ROLE_1"})
    public Msg updateSale(@PathVariable Long saleId, @RequestBody @Validated SaleParam param) {
        saleService.updateSale(saleId, param);
        return Msg.success("修改成功");
    }

    @ApiOperation("删除销售地点（管理员）")
    @DeleteMapping("/{saleId}")
    @RolesAllowed({"ROLE_1"})
    public Msg deleteSale(@PathVariable Long saleId) {
        saleService.deleteSale(saleId);
        return Msg.success("删除成功");
    }
}
