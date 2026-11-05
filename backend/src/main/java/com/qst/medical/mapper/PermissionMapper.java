package com.qst.medical.mapper;

import com.qst.medical.domain.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单权限查询。
 */
@Mapper
public interface PermissionMapper {

    /**
     * 按角色查该角色拥有的全部菜单行（未成树）。
     *
     * @param roleName 形如 ROLE_1 / ROLE_2
     * @return 菜单行，按 id 升序
     */
    List<Permission> selectPermissionByRole(@Param("roleName") String roleName);
}
