package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 修改密码入参（当前登录用户改自己的口令）。
 */
@Data
@ApiModel("修改密码入参")
public class PasswordParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "原密码", required = true)
    @NotBlank(message = "请输入原密码")
    private String oldPwd;

    @ApiModelProperty(value = "新密码", required = true)
    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 20, message = "新密码长度需在 6-20 位之间")
    private String newPwd;

    @ApiModelProperty(value = "确认新密码", required = true)
    @NotBlank(message = "请再次输入新密码")
    private String confirmPwd;
}
