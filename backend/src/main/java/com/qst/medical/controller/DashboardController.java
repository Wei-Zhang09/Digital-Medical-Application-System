package com.qst.medical.controller;

import com.qst.medical.common.Msg;
import com.qst.medical.service.DashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;

/**
 * 首页数据面板：管理员与医师都能看（官方扩展需求）。
 */
@Api(tags = "数据面板")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @ApiOperation("首页统计：数量、职称柱状图、科室饼图、最新资讯")
    @GetMapping
    @RolesAllowed({"ROLE_1", "ROLE_2"})
    public Msg getDashboard() {
        return Msg.success().data("dashboard", dashboardService.getDashboard());
    }

    @ApiOperation("首页资讯详情（点开最新资讯里的某一条）")
    @GetMapping("/news/{source}/{id}")
    @RolesAllowed({"ROLE_1", "ROLE_2"})
    public Msg getNewsDetail(@PathVariable String source, @PathVariable Long id) {
        return Msg.success().data("news", dashboardService.getNewsDetail(source, id));
    }
}
