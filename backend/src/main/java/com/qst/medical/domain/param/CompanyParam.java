package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel("医药公司入参")
public class CompanyParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "公司名称", required = true)
    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    @ApiModelProperty(value = "公司电话", required = true)
    @NotBlank(message = "公司电话不能为空")
    private String companyPhone;
}
