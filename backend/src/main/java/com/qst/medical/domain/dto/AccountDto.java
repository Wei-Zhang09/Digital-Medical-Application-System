package com.qst.medical.domain.dto;

import com.qst.medical.domain.dataobject.AccountDo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 用户前端数据交互实体（第三层，继承 {@link AccountDo} 并实现 {@link UserDetails}）。
 *
 * <p>Spring Security 直接把它当作用户凭据：登录时用它校验密码，
 * JwtFilter 校验通过后也用它作为 SecurityContext 的 principal（此时只填 id/uname/role）。</p>
 */
public class AccountDto extends AccountDo implements UserDetails {

    private static final long serialVersionUID = 1L;

    /** 权限：单角色，直接由角色名构造 ROLE_1 / ROLE_2 */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = getRole() != null ? getRole() : getUtype();
        if (roleName == null || roleName.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return getPwd();
    }

    @Override
    public String getUsername() {
        return getUname();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
