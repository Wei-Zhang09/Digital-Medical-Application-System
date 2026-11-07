/*
 * 给医师角色（ROLE_2）补齐三个只读菜单：医药公司管理 / 销售地点管理 / 城市信息管理
 *
 * 背景：官方 bin_text.sql 的 role_permission 只给 ROLE_2 五个菜单
 * （首页、药品、医保政策、公司政策、必备材料），
 * 而《项目需求描述》1.3 写的是「医师可以查看医药公司、公司政策、城市信息、
 * 医保政策信息、销售地点信息、药品信息和必备材料信息」——两边不一致。
 * 本项目按需求文档对齐：补 3 个菜单，只给查看，写操作仍限 ROLE_1。
 *
 * 菜单标题由后端 PermissionService 统一把「管理」改写成「查询」，
 * 所以这里只需要加 role_permission 关联，不用改 permission 表。
 *
 * 幂等：先删后插，可重复执行。
 */

USE digital_medical_application_system;

DELETE FROM role_permission WHERE roleName = 'ROLE_2' AND per_id IN (3, 4, 5);

INSERT INTO role_permission (roleName, per_id) VALUES
  ('ROLE_2', 3),   -- BaseCompany 医药公司
  ('ROLE_2', 4),   -- BaseSale    销售地点
  ('ROLE_2', 5);   -- BaseCity    城市信息

-- 校验：ROLE_2 现在应有 8 条（含 Layout 与 404 之外的业务菜单）
SELECT rp.roleName, COUNT(*) AS menu_count
FROM role_permission rp
WHERE rp.roleName = 'ROLE_2'
GROUP BY rp.roleName;
