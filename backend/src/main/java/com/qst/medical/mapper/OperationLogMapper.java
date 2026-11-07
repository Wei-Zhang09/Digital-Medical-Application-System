package com.qst.medical.mapper;

import com.qst.medical.domain.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperationLogMapper {

    int insert(OperationLog log);

    /** 分页查询，keyword 模糊匹配 操作人/模块/动作 */
    List<OperationLog> selectLogPage(@Param("keyword") String keyword);
}
