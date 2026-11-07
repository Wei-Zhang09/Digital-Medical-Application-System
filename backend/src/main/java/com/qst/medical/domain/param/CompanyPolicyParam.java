package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("医药公司政策入参")
public class CompanyPolicyParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "政策标题", required = true)
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多 100 个字符")
    private String title;

    @ApiModelProperty(value = "政策内容", required = true)
    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容最多 2000 个字符")
    private String message;

    @ApiModelProperty(value = "所属医药公司 id", required = true)
    @NotNull(message = "必须选择医药公司")
    private Long companyId;
}
