package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 必备材料的新增/修改入参。
 */
@Data
@ApiModel("必备材料入参")
public class MaterialParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "材料标题", required = true)
    @NotBlank(message = "标题不能为空")
    private String title;

    @ApiModelProperty(value = "材料内容", required = true)
    @NotBlank(message = "内容不能为空")
    private String message;
}
