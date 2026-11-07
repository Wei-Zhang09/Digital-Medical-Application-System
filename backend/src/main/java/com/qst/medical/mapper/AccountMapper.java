package com.qst.medical.mapper;

import com.qst.medical.domain.dataobject.AccountDo;
import com.qst.medical.domain.dto.AccountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 mapper：登录查询 + 医师模块需要的账号维护（新增、改资料、重置口令、删除）。
 */
@Mapper
public interface AccountMapper {

    /**
     * 按登录名查询用户，用于登录认证。
     *
     * @param uname 登录名
     * @return 带角色信息的用户，查不到返回 null
     */
    AccountDto selectAccountByUname(@Param("uname") String uname);

    /** 登录名是否已被占用 */
    int countByUname(@Param("uname") String uname);

    AccountDo selectById(@Param("id") Long id);

    int insertAccount(AccountDo account);

    /** 只更新姓名与手机号（医师资料变更时同步账号） */
    int updateAccountBasic(AccountDo account);

    /** 重置口令（传 BCrypt 密文） */
    int updatePwd(@Param("id") Long id, @Param("pwd") String pwd);

    int deleteById(@Param("id") Long id);

    /** 账号列表（keyword 模糊匹配登录名/姓名，null 表示不过滤） */
    java.util.List<AccountDo> selectAccountPage(@Param("keyword") String keyword);

    /** 某角色下的账号数量（用于保护最后一个管理员） */
    int countByUtype(@Param("utype") String utype);

    /** 修改姓名、手机号、角色（登录名与口令不在此更新） */
    int updateAccountBasicInfo(AccountDo account);
}
