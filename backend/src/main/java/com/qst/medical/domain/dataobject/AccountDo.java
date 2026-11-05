package com.qst.medical.domain.dataobject;

import com.qst.medical.domain.entity.Account;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户数据库操作实体（第二层，继承 {@link Account}）。
 *
 * <p>在表字段之上补充「角色名」字段 role：数据库里角色存在 account.utype 列，
 * 查询时用 {@code utype AS role} 映射过来，让上层不用关心列名。</p>
 *
 * <p>注：官方任务书写作 {@code domain/do} 包，但 {@code do} 是 Java 关键字不能作包名，
 * 这里改名为 {@code domain.dataobject}，类名仍保持 AccountDo。</p>
 */
@Getter
@Setter
public class AccountDo extends Account {

    private static final long serialVersionUID = 1L;

    /** 角色名（ROLE_1 / ROLE_2），来自 account.utype */
    private String role;
}
