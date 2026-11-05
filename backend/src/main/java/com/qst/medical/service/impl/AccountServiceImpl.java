package com.qst.medical.service.impl;

import com.qst.medical.domain.dto.AccountDto;
import com.qst.medical.mapper.AccountMapper;
import com.qst.medical.service.AccountService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户服务实现：走 AccountMapper 查库。
 */
@Service
public class AccountServiceImpl implements AccountService {

    @Resource
    private AccountMapper accountMapper;

    @Override
    public AccountDto getAccountByUname(String uname) {
        if (uname == null || uname.trim().isEmpty()) {
            return null;
        }
        return accountMapper.selectAccountByUname(uname.trim());
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
