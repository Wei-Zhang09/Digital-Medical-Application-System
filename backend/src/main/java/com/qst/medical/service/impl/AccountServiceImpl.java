package com.qst.medical.service.impl;

import com.github.pagehelper.PageHelper;
import com.qst.medical.common.PageBean;
import com.qst.medical.common.PageUtil;
import com.qst.medical.common.SortUtil;
import com.qst.medical.domain.dataobject.AccountDo;
import com.qst.medical.domain.param.AccountParam;
import com.qst.medical.domain.dto.AccountDto;
import com.qst.medical.domain.param.PasswordParam;
import com.qst.medical.domain.param.RegisterParam;
import com.qst.medical.mapper.AccountMapper;
import com.qst.medical.service.AccountService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 用户服务实现：走 AccountMapper 查库。
 */
@Service
public class AccountServiceImpl implements AccountService {

    /** 自助注册的账号统一为医师角色 */
    private static final String ROLE_DOCTOR = "ROLE_2";

    /** 管理员角色 */
    private static final String ROLE_ADMIN = "ROLE_1";

    /** 新增/重置口令时的初始口令 */
    private static final String DEFAULT_PASSWORD = "123456";

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public AccountDto getAccountByUname(String uname) {
        if (uname == null || uname.trim().isEmpty()) {
            return null;
        }
        return accountMapper.selectAccountByUname(uname.trim());
    }

    @Override
    public void register(RegisterParam param) {
        if (!param.getPwd().equals(param.getConfirmPwd())) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        String uname = param.getUname().trim();
        if (accountMapper.countByUname(uname) > 0) {
            throw new IllegalArgumentException("登录名已被占用：" + uname);
        }

        Date now = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        AccountDo account = new AccountDo();
        account.setUname(uname);
        account.setRealname(param.getRealname().trim());
        account.setPhonenumber(param.getPhonenumber().trim());
        account.setPwd(passwordEncoder.encode(param.getPwd()));
        account.setUtype(ROLE_DOCTOR);
        account.setCreatetime(now);
        account.setUpdatetime(now);
        accountMapper.insertAccount(account);
    }

    @Override
    public void changePassword(Long accountId, PasswordParam param) {
        AccountDo account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("账号不存在");
        }
        if (!passwordEncoder.matches(param.getOldPwd(), account.getPwd())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        if (!param.getNewPwd().equals(param.getConfirmPwd())) {
            throw new IllegalArgumentException("两次输入的新密码不一致");
        }
        if (param.getOldPwd().equals(param.getNewPwd())) {
            throw new IllegalArgumentException("新密码不能与原密码相同");
        }
        accountMapper.updatePwd(accountId, passwordEncoder.encode(param.getNewPwd()));
    }

    @Override
    public PageBean getAccountList(Integer pageNum, Integer pageSize, String keyword, String sort, String order) {
        PageHelper.startPage(pageNum, pageSize, SortUtil.build("account", sort, order));
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        List<AccountDo> list = accountMapper.selectAccountPage(kw);
        return PageUtil.toPageBean(list);
    }

    @Override
    public void addAccount(AccountParam param) {
        String uname = param.getUname() == null ? "" : param.getUname().trim();
        if (uname.isEmpty()) {
            throw new IllegalArgumentException("登录名不能为空");
        }
        if (accountMapper.countByUname(uname) > 0) {
            throw new IllegalArgumentException("登录名已存在：" + uname);
        }
        Date now = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        AccountDo account = new AccountDo();
        account.setUname(uname);
        account.setRealname(param.getRealname().trim());
        account.setPhonenumber(param.getPhonenumber().trim());
        account.setUtype(param.getUtype());
        account.setPwd(passwordEncoder.encode(DEFAULT_PASSWORD));
        account.setCreatetime(now);
        account.setUpdatetime(now);
        accountMapper.insertAccount(account);
    }

    @Override
    public void updateAccount(Long id, AccountParam param) {
        AccountDo account = accountMapper.selectById(id);
        if (account == null) {
            throw new IllegalArgumentException("账号不存在");
        }
        // 保护最后一个管理员：不能把唯一的管理员降级成医师
        if (ROLE_ADMIN.equals(account.getUtype()) && !ROLE_ADMIN.equals(param.getUtype())
                && accountMapper.countByUtype(ROLE_ADMIN) <= 1) {
            throw new IllegalArgumentException("这是最后一个管理员账号，不能改为医师");
        }
        account.setRealname(param.getRealname().trim());
        account.setPhonenumber(param.getPhonenumber().trim());
        account.setUtype(param.getUtype());
        account.setUpdatetime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        accountMapper.updateAccountBasicInfo(account);
    }

    @Override
    public void resetPassword(Long id) {
        AccountDo account = accountMapper.selectById(id);
        if (account == null) {
            throw new IllegalArgumentException("账号不存在");
        }
        accountMapper.updatePwd(id, passwordEncoder.encode(DEFAULT_PASSWORD));
    }

    @Override
    public void deleteAccount(Long id, Long currentId) {
        AccountDo account = accountMapper.selectById(id);
        if (account == null) {
            throw new IllegalArgumentException("账号不存在");
        }
        if (id.equals(currentId)) {
            throw new IllegalArgumentException("不能删除当前登录的账号");
        }
        if (ROLE_ADMIN.equals(account.getUtype()) && accountMapper.countByUtype(ROLE_ADMIN) <= 1) {
            throw new IllegalArgumentException("这是最后一个管理员账号，不能删除");
        }
        accountMapper.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String uname) throws UsernameNotFoundException {
        AccountDto account = getAccountByUname(uname);
        if (account == null) {
            // 交给 Spring Security 统一转成 BadCredentialsException（默认隐藏"用户不存在"）
            throw new UsernameNotFoundException("用户不存在：" + uname);
        }
        return account;
    }
}
