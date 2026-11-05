package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel("销售地点入参")
public class SaleParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "药店名称", required = true)
    @NotBlank(message = "药店名称不能为空")
    private String saleName;

    @ApiModelProperty(value = "联系电话", required = true)
    @NotBlank(message = "联系电话不能为空")
    private String salePhone;

    @ApiModelProperty("经度（地图落点）")
    private Double lng;

    @ApiModelProperty("纬度（地图落点）")
    private Double lat;

    @ApiModelProperty("门店地址")
    private String address;
}
