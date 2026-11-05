package com.qst.medical.mapper;

import com.qst.medical.domain.entity.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaterialMapper {

    /** 按标题模糊查询（标题为空则查全部），配合 PageHelper 分页 */
    List<Material> selectMaterial(@Param("title") String title);

    Material selectById(@Param("id") Integer id);

    int insertMaterial(Material material);

    int updateMaterial(Material material);

    int deleteById(@Param("id") Integer id);
}
