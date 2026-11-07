package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
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
    @Size(max = 100, message = "标题最多 100 个字符")
    private String title;

    @ApiModelProperty(value = "材料内容", required = true)
    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容最多 2000 个字符")
    private String message;
}
