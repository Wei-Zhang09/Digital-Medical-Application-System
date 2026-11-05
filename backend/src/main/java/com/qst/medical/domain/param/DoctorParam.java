package com.qst.medical.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 医师新增/修改入参。
 *
 * <p>{@code uname} 不填时后端按「姓名 + 手机号后 4 位」生成，与官方示例账号
 * （王小明4125）保持一致；口令统一初始化为 123456。</p>
 */
@Data
@ApiModel("医师入参")
public class DoctorParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "姓名", required = true)
    @NotBlank(message = "姓名不能为空")
    private String name;

    @ApiModelProperty(value = "年龄", required = true)
    @NotNull(message = "年龄不能为空")
    @Min(value = 18, message = "年龄不能小于 18")
    @Max(value = 100, message = "年龄不能大于 100")
    private Integer age;

    @ApiModelProperty(value = "性别：1 男，2 女", required = true)
    @NotNull(message = "性别不能为空")
    private Integer sex;

    @ApiModelProperty(value = "医师级别 id", required = true)
    @NotNull(message = "请选择医师级别")
    private Long levelId;

    @ApiModelProperty(value = "联系电话", required = true)
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @ApiModelProperty(value = "诊治类型 id", required = true)
    @NotNull(message = "请选择诊治类型")
    private Long typeId;

    @ApiModelProperty(value = "所属医院", required = true)
    @NotBlank(message = "所属医院不能为空")
    private String hospital;

    @ApiModelProperty("登录名（不填则自动生成）")
    private String uname;
}
