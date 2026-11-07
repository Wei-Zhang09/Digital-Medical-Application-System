package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 账号管理入参（管理员维护用户账号）。
 *
 * <p>登录名只在新增时使用，修改时不可改（避免把历史数据的关联打断）。</p>
 */
@Data
@ApiModel("账号管理入参")
public class AccountParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "登录名（仅新增时使用）", example = "zhangsan01")
    @Size(max = 20, message = "登录名最多 20 个字符")
    private String uname;

    @ApiModelProperty(value = "真实姓名", required = true)
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 20, message = "真实姓名最多 20 个字符")
    private String realname;

    @ApiModelProperty(value = "手机号", required = true, example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phonenumber;

    @ApiModelProperty(value = "角色：ROLE_1 管理员 / ROLE_2 医师", required = true)
    @NotBlank(message = "请选择角色")
    @Pattern(regexp = "^ROLE_[12]$", message = "角色只能是 ROLE_1 或 ROLE_2")
    private String utype;
}
