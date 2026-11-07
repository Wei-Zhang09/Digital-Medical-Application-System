package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel("销售地点入参")
public class SaleParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "药店名称", required = true)
    @NotBlank(message = "药店名称不能为空")
    @Size(max = 50, message = "药店名称最多 50 个字符")
    private String saleName;

    @ApiModelProperty(value = "联系电话", required = true)
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^(1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})$", message = "电话格式不正确（手机 11 位或固话带区号）")
    private String salePhone;

    @ApiModelProperty("经度（地图落点）")
    @DecimalMin(value = "-180", message = "经度范围 -180 ~ 180")
    @DecimalMax(value = "180", message = "经度范围 -180 ~ 180")
    private Double lng;

    @ApiModelProperty("纬度（地图落点）")
    @DecimalMin(value = "-90", message = "纬度范围 -90 ~ 90")
    @DecimalMax(value = "90", message = "纬度范围 -90 ~ 90")
    private Double lat;

    @ApiModelProperty("门店地址")
    @Size(max = 120, message = "地址最多 120 个字符")
    private String address;
}
