package com.qst.medical.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 药品实体，对应 bin_text.drug；与药店是多对多，靠 drug_sale 关联。
 */
@Data
public class Drug implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long drugId;
    private String drugName;
    /** 药品成分 */
    private String drugInfo;
    /** 药品功能 */
    private String drugEffect;
    /** 图片地址，形如 /image/xxx.jpg */
    private String drugImg;
    private String publisher;
    private LocalDateTime createtime;
    private LocalDateTime updatetime;

    /** 关联的销售地点 id（新增/修改时提交，不入 drug 表） */
    private List<Long> saleIds;
    /** 展示用：关联药店名称，逗号拼接 */
    private String saleNames;
}
