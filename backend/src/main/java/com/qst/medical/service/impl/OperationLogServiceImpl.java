package com.qst.medical.service.impl;

import com.github.pagehelper.PageHelper;
import com.qst.medical.common.PageBean;
import com.qst.medical.common.PageUtil;
import com.qst.medical.mapper.OperationLogMapper;
import com.qst.medical.service.OperationLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public PageBean getLogList(Integer pageNum, Integer pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        return PageUtil.toPageBean(operationLogMapper.selectLogPage(kw));
    }
}
