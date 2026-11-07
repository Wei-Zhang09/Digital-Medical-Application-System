package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Min;

/**
 * 操作日志查询（仅管理员）。
 */
@Api(tags = "操作日志")
@RestController
@RequestMapping("/api/logs")
public class OperationLogController {

    @Resource
    private OperationLogService operationLogService;

    @ApiOperation("操作日志分页列表（管理员）")
    @GetMapping("/{pageNum}/{pageSize}")
    @RolesAllowed({"ROLE_1"})
    public Msg getLogList(@PathVariable @Min(1) Integer pageNum,
                          @PathVariable @Min(1) Integer pageSize,
                          @RequestParam(required = false) String keyword) {
        return Msg.success().data("logPageInfo", operationLogService.getLogList(pageNum, pageSize, keyword));
    }
}
