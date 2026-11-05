package com.qst.medical.common;

import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 把 PageHelper 的 PageInfo 转成本项目的 {@link PageBean}。
 *
 * <p>各模块的列表接口都走这里，保证前端拿到的分页结构完全一致：
 * {@code data.xxxPageInfo = {pageNum,pageSize,total,pages,list}}。</p>
 */
public final class PageUtil {

    private PageUtil() {
    }

    public static PageBean toPageBean(List<?> list) {
        PageInfo<?> info = new PageInfo<>(list);
        PageBean bean = new PageBean();
        bean.setPageNum(info.getPageNum());
        bean.setPageSize(info.getPageSize());
        bean.setTotal(info.getTotal());
        bean.setPages(info.getPages());
        bean.setList(info.getList());
        return bean;
    }
}
