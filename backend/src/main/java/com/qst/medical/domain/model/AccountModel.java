package com.qst.medical.domain.model;

import com.qst.medical.domain.dto.AccountDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 用户 model 类（第四层）：只保留返回给前端的字段，避免把密码等敏感信息序列化出去。
 *
 * <p>utype 是去掉 {@code ROLE_} 前缀的短码（"1" 管理员 / "2" 医师），前端拿它做菜单与按钮判断；
 * role 保留完整角色名（ROLE_1 / ROLE_2），供 /api/permissions 等接口使用。</p>
 */
@Getter
@Setter
public class AccountModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String realname;
    private String uname;
    private String phonenumber;
    /** 短码角色：1 / 2 */
    private String utype;
    /** 完整角色名：ROLE_1 / ROLE_2 */
    private String role;

    public static AccountModel from(AccountDto dto) {
        if (dto == null) {
            return null;
        }
        AccountModel model = new AccountModel();
        model.setId(dto.getId());
        model.setRealname(dto.getRealname());
        model.setUname(dto.getUname());
        model.setPhonenumber(dto.getPhonenumber());
        String role = dto.getRole() != null ? dto.getRole() : dto.getUtype();
        model.setRole(role);
        model.setUtype(shortRole(role));
        return model;
    }

    /** ROLE_1 -> 1 */
    private static String shortRole(String role) {
        if (role == null) {
            return null;
        }
        return role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role;
    }
}
