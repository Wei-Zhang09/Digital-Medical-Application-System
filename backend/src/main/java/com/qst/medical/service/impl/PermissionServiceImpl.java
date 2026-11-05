package com.qst.medical.service.impl;

import com.qst.medical.domain.entity.Permission;
import com.qst.medical.domain.model.PermissionMetaModel;
import com.qst.medical.domain.model.PermissionModel;
import com.qst.medical.mapper.PermissionMapper;
import com.qst.medical.service.PermissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单权限实现：把平铺的菜单行按 pid 组装成树。
 *
 * <p>官方任务 19 的做法是「查全部 → 找 pid=0 的根 → 递归挂 children」，
 * 这里保持一致；医师角色（ROLE_2）只读，菜单标题里的「管理」换成「查询」。</p>
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    /** 管理员角色：不改菜单标题 */
    private static final String ROLE_ADMIN = "ROLE_1";

    @Resource
    private PermissionMapper permissionMapper;

    @Override
    public List<PermissionModel> getAllPermission(String roleName) {
        List<PermissionModel> result = new ArrayList<>();
        if (roleName == null || roleName.trim().isEmpty()) {
            return result;
        }
        String finalRoleName = roleName.trim();
        List<Permission> all = permissionMapper.selectPermissionByRole(finalRoleName);
        if (all == null || all.isEmpty()) {
            return result;
        }
        // 先转成 model，避免递归里反复 new
        List<PermissionModel> models = new ArrayList<>(all.size());
        for (Permission p : all) {
            models.add(toModel(p, finalRoleName));
        }
        // pid = 0 的是根（Layout / 404）
        for (PermissionModel node : models) {
            if (node.getPid() != null && node.getPid() == 0) {
                node.setChildren(selectChildren(node, models));
                result.add(node);
            }
        }
        return result;
    }

    /**
     * 递归挂子节点。
     */
    private List<PermissionModel> selectChildren(PermissionModel father, List<PermissionModel> all) {
        List<PermissionModel> children = new ArrayList<>();
        for (PermissionModel item : all) {
            if (item.getPid() != null && item.getPid().equals(father.getId())) {
                item.setChildren(selectChildren(item, all));
                children.add(item);
            }
        }
        return children;
    }

    private PermissionModel toModel(Permission p, String roleName) {
        PermissionModel m = new PermissionModel();
        m.setId(p.getId());
        m.setPid(p.getPid());
        m.setName(p.getName());
        m.setPath(p.getPath());
        m.setComponent(p.getComponent());
        m.setLevel(p.getLevel());
        String title = p.getTitle() == null ? "" : p.getTitle();
        if (!ROLE_ADMIN.equals(roleName)) {
            title = title.replace("管理", "查询");
        }
        m.setTitle(title);
        PermissionMetaModel meta = new PermissionMetaModel();
        meta.setTitle(title);
        m.setMeta(meta);
        return m;
    }
}
