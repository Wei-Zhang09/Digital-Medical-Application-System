package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 注册入参。
 *
 * <p>注册只创建登录账号（account 表），角色固定为 ROLE_2（医师），
 * 不写入 doctor 表——医师档案由管理员在「医生信息管理」里补全。</p>
 */
@Data
@ApiModel("注册入参")
public class RegisterParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "登录名", required = true, example = "zhangsan01")
    @NotBlank(message = "登录名不能为空")
    @Size(min = 4, max = 20, message = "登录名长度需在 4-20 个字符之间")
    @Pattern(regexp = "^[A-Za-z0-9_\\u4e00-\\u9fa5]+$", message = "登录名只能包含字母、数字、下划线或汉字")
    private String uname;

    @ApiModelProperty(value = "真实姓名", required = true, example = "张三")
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 20, message = "真实姓名最多 20 个字符")
    private String realname;

    @ApiModelProperty(value = "手机号", required = true, example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phonenumber;

    @ApiModelProperty(value = "密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在 6-20 位之间")
    private String pwd;

    @ApiModelProperty(value = "确认密码", required = true, example = "123456")
    @NotBlank(message = "请再次输入密码")
    private String confirmPwd;
}
