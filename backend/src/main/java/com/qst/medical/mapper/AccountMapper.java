package com.qst.medical.mapper;

import com.qst.medical.domain.dto.AccountDto;
import org.apache.ibatis.annotations.Param;

/**
 * 用户登录查询 mapper（对应 resources/mapper/AccountMapper.xml）。
 */
public interface AccountMapper {

    /**
     * 按登录名查询用户，用于登录认证。
     *
     * @param uname 登录名
     * @return 带角色信息的用户，查不到返回 null
     */
    AccountDto selectAccountByUname(@Param("uname") String uname);
}
