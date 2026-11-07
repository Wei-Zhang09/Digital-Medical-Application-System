package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("药品入参")
public class DrugParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "药名", required = true)
    @NotBlank(message = "药名不能为空")
    @Size(max = 60, message = "药名最多 60 个字符")
    private String drugName;

    @ApiModelProperty(value = "药品成分", required = true)
    @NotBlank(message = "药品成分不能为空")
    @Size(max = 500, message = "药品成分最多 500 个字符")
    private String drugInfo;

    @ApiModelProperty(value = "药品功能", required = true)
    @NotBlank(message = "药品功能不能为空")
    @Size(max = 500, message = "药品功能最多 500 个字符")
    private String drugEffect;

    @ApiModelProperty("图片地址（上传接口返回的 /image/xxx）")
    private String drugImg;

    @ApiModelProperty("关联的销售地点 id 列表")
    private List<Long> saleIds;
}
