/*
 * 账号管理菜单（需求 1.2「用户管理模块」）
 *
 * 只给管理员（ROLE_1）。医师角色不加，避免越权看到全站账号。
 * 幂等：先按 name 删旧行再插入，可重复执行。
 */

USE digital_medical_application_system;

DELETE FROM role_permission
WHERE per_id IN (SELECT id FROM permission WHERE name = 'AccountManage');

DELETE FROM permission WHERE name = 'AccountManage';

INSERT INTO permission (pid, name, path, component, level, title)
VALUES (1, 'AccountManage', '/manage/account', 'AccountManage', 1, '账号管理');

INSERT INTO role_permission (roleName, per_id)
SELECT 'ROLE_1', id FROM permission WHERE name = 'AccountManage';

SELECT p.id, p.title, p.path
FROM permission p
WHERE p.name = 'AccountManage';
