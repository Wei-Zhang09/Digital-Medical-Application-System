package com.qst.medical.service;

import com.qst.medical.common.PageBean;

/**
 * 操作日志查询服务（只读，日志由切面写入）。
 */
public interface OperationLogService {

    PageBean getLogList(Integer pageNum, Integer pageSize, String keyword);
}
