package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel("医药公司入参")
public class CompanyParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "公司名称", required = true)
    @NotBlank(message = "公司名称不能为空")
    @Size(max = 50, message = "公司名称最多 50 个字符")
    private String companyName;

    @ApiModelProperty(value = "公司电话", required = true)
    @NotBlank(message = "公司电话不能为空")
    @Pattern(regexp = "^(1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})$", message = "电话格式不正确（手机 11 位或固话带区号）")
    private String companyPhone;
}
