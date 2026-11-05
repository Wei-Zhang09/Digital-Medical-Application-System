package com.qst.medical.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询公共类。
 *
 * <p>所有需要分页的实体类都继承本类（官方任务 1 步骤 1-2），
 * 分页数据由 PageHelper 填充后封装成 {@code Msg.data("xxxPageInfo", pageBean)} 返回前端。</p>
 */
@Getter
@Setter
public class PageBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页码，从 1 开始 */
    private Integer pageNum;
    /** 每页条数 */
    private Integer pageSize;
    /** 总记录数 */
    private Long total;
    /** 总页数 */
    private Integer pages;
    /** 当前页数据 */
    private List<?> list;
}
