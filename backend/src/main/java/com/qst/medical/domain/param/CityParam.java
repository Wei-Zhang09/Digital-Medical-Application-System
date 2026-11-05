package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 城市新增入参。城市只支持新增、查询、删除（官方需求），不做修改。
 */
@Data
@ApiModel("城市入参")
public class CityParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "城市编号", required = true)
    @NotNull(message = "城市编号不能为空")
    private Integer cityNumber;
}
