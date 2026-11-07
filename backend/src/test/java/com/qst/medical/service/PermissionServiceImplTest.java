package com.qst.medical.service;

import com.qst.medical.domain.entity.Permission;
import com.qst.medical.domain.model.PermissionModel;
import com.qst.medical.mapper.PermissionMapper;
import com.qst.medical.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 菜单权限树单元测试：树组装 + 医师角色标题替换。
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private List<Permission> allPermissions;

    @BeforeEach
    void setUp() {
        // 模拟 digital_medical_application_system.permission 的 11 行：Layout(1) + 9 个业务菜单(2-10) + 404(11)
        allPermissions = new ArrayList<>();
        allPermissions.add(permission(1, 0, "Layout", "/", "Layout", "Layout"));
        String[][] menus = {
                {"2", "Home", "/home", "Home", "首页"},
                {"3", "BaseCompany", "/base/company", "CompanyManage", "医药公司管理"},
                {"4", "BaseSale", "/base/sale", "SaleManage", "销售地点管理"},
                {"5", "BaseCity", "/base/city", "CityManage", "城市信息管理"},
                {"6", "ManageDrug", "/manage/drug", "DrugManage", "药品信息管理"},
                {"7", "MedicalPolicy", "/manage/medical/policy", "MedicalPolicy", "医保政策管理"},
                {"8", "CompanyPolicy", "/manage/company/policy", "CompanyPolicy", "医药公司政策管理"},
                {"9", "DoctorManage", "/manage/doctor", "DoctorManage", "医生信息管理"},
                {"10", "MaterialManage", "/manage/material", "MaterialManage", "必备材料管理"}
        };
        for (String[] m : menus) {
            allPermissions.add(permission(Integer.parseInt(m[0]), 1, m[1], m[2], m[3], m[4]));
        }
        allPermissions.add(permission(11, 0, "error-404", "/*", "404", "404-页面不存在"));
    }

    @Test
    @DisplayName("管理员拿到完整菜单树：两个根，Layout 下 9 项")
    void getAllPermission_adminShouldGetFullTree() {
        when(permissionMapper.selectPermissionByRole("ROLE_1")).thenReturn(allPermissions);

        List<PermissionModel> tree = permissionService.getAllPermission("ROLE_1");

        assertEquals(2, tree.size(), "根节点应有 Layout 与 404 两个");
        PermissionModel layout = tree.stream().filter(p -> "Layout".equals(p.getComponent())).findFirst().orElse(null);
        assertNotNull(layout);
        assertEquals(9, layout.getChildren().size());
        assertEquals("首页", layout.getChildren().get(0).getTitle());
        assertEquals("医药公司管理", layout.getChildren().get(1).getTitle());
        // meta 里也要有标题，前端两种写法都兼容
        assertEquals("首页", layout.getChildren().get(0).getMeta().getTitle());
    }

    @Test
    @DisplayName("医师只拿到 5 项，且标题「管理」变「查询」")
    void getAllPermission_doctorShouldGetFiveWithQueryTitle() {
        // ROLE_2 授权：1,2,6,7,8,10,11
        List<Permission> doctorPermissions = Arrays.asList(
                allPermissions.get(0),   // Layout
                allPermissions.get(1),   // 首页
                allPermissions.get(5),   // 药品
                allPermissions.get(6),   // 医保政策
                allPermissions.get(7),   // 公司政策
                allPermissions.get(9),   // 必备材料
                allPermissions.get(10)   // 404
        );
        when(permissionMapper.selectPermissionByRole("ROLE_2")).thenReturn(doctorPermissions);

        List<PermissionModel> tree = permissionService.getAllPermission("ROLE_2");
        PermissionModel layout = tree.get(0);

        assertEquals(5, layout.getChildren().size());
        List<String> titles = new ArrayList<>();
        layout.getChildren().forEach(c -> titles.add(c.getTitle()));
        assertTrue(titles.contains("首页"));
        assertTrue(titles.contains("药品信息查询"));
        assertTrue(titles.contains("医保政策查询"));
        assertTrue(titles.contains("医药公司政策查询"));
        assertTrue(titles.contains("必备材料查询"));
        assertFalse(titles.contains("药品信息管理"), "医师不应看到「管理」字样");
    }

    @Test
    @DisplayName("角色为空或菜单为空时返回空列表")
    void getAllPermission_shouldReturnEmpty() {
        assertTrue(permissionService.getAllPermission(null).isEmpty());
        assertTrue(permissionService.getAllPermission("  ").isEmpty());

        when(permissionMapper.selectPermissionByRole(anyString())).thenReturn(Collections.emptyList());
        assertTrue(permissionService.getAllPermission("ROLE_9").isEmpty());
    }

    private Permission permission(int id, int pid, String name, String path, String component, String title) {
        Permission p = new Permission();
        p.setId(id);
        p.setPid(pid);
        p.setName(name);
        p.setPath(path);
        p.setComponent(component);
        p.setLevel(pid == 0 ? 0 : 1);
        p.setTitle(title);
        return p;
    }
}
