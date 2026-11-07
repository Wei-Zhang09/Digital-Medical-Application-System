package com.qst.medical.service;

import com.qst.medical.common.PageBean;
import com.qst.medical.domain.dto.AccountDto;
import com.qst.medical.domain.param.AccountParam;
import com.qst.medical.domain.param.PasswordParam;
import com.qst.medical.domain.param.RegisterParam;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户服务：负责登录用户校验与角色获取。
 *
 * <p>继承 {@link UserDetailsService}，直接交给 Spring Security 的 DaoAuthenticationProvider 使用，
 * 登录接口 /api/login 由 formLogin 处理，不需要单独写 Controller。</p>
 */
public interface AccountService extends UserDetailsService {

    /**
     * 按登录名查询用户（含密码与角色），供认证与 JwtFilter 复用。
     *
     * @param uname 登录名
     * @return 用户，不存在返回 null
     */
    AccountDto getAccountByUname(String uname);

    /**
     * 自助注册：创建登录账号，角色固定 ROLE_2。
     *
     * @param param 注册入参（已通过 Bean Validation）
     * @throws IllegalArgumentException 登录名已存在 / 两次密码不一致
     */
    void register(RegisterParam param);

    /**
     * 修改当前登录用户的口令。
     *
     * @param accountId 当前登录用户 id（来自 JWT，不接受前端传参）
     * @param param     原密码 / 新密码 / 确认新密码
     * @throws IllegalArgumentException 原密码错误 / 两次新密码不一致 / 新旧密码相同
     */
    void changePassword(Long accountId, PasswordParam param);

    /** 账号分页列表（keyword 模糊匹配登录名/姓名） */
    PageBean getAccountList(Integer pageNum, Integer pageSize, String keyword, String sort, String order);

    /**
     * 新增账号（初始口令 123456）。
     *
     * @throws IllegalArgumentException 登录名已存在
     */
    void addAccount(AccountParam param);

    /**
     * 修改账号资料与角色。
     *
     * @throws IllegalArgumentException 账号不存在 / 不允许把最后一个管理员降级
     */
    void updateAccount(Long id, AccountParam param);

    /** 重置口令为 123456 */
    void resetPassword(Long id);

    /**
     * 删除账号。
     *
     * @param id        被删账号
     * @param currentId 当前登录账号（不允许删自己）
     * @throws IllegalArgumentException 不能删自己 / 不能删最后一个管理员
     */
    void deleteAccount(Long id, Long currentId);
}
