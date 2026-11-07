package com.qst.medical.service;

import com.qst.medical.domain.model.DashboardModel;

import java.util.Map;

/**
 * 首页数据面板服务。
 */
public interface DashboardService {

    DashboardModel getDashboard();

    /**
     * 首页资讯详情。
     *
     * @param source 来源代码：medical / company / material
     * @param id     对应表的 id
     * @return 含 id/title/content/time/source/modulePath 的 Map
     * @throws IllegalArgumentException 来源非法或记录不存在
     */
    Map<String, Object> getNewsDetail(String source, Long id);
}
