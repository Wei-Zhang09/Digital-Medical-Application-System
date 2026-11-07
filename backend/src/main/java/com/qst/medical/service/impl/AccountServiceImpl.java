package com.qst.medical.service.impl;

import com.qst.medical.domain.dataobject.AccountDo;
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

/**
 * 用户服务实现：走 AccountMapper 查库。
 */
@Service
public class AccountServiceImpl implements AccountService {

    /** 自助注册的账号统一为医师角色 */
    private static final String ROLE_DOCTOR = "ROLE_2";

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
    public UserDetails loadUserByUsername(String uname) throws UsernameNotFoundException {
        AccountDto account = getAccountByUname(uname);
        if (account == null) {
            // 交给 Spring Security 统一转成 BadCredentialsException（默认隐藏"用户不存在"）
            throw new UsernameNotFoundException("用户不存在：" + uname);
        }
        return account;
    }
}
