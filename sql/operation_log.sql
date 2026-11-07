/*
 * 操作日志：建表 + 菜单（仅管理员）
 *
 * 日志由后端切面自动写入（POST/PUT/DELETE 成功后落一条），
 * 这里只负责表和菜单。幂等：可重复执行。
 */

USE digital_medical_application_system;

CREATE TABLE IF NOT EXISTS operation_log (
  id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  uname      VARCHAR(50)  DEFAULT NULL COMMENT '操作人登录名',
  realname   VARCHAR(50)  DEFAULT NULL COMMENT '操作人姓名',
  module     VARCHAR(50)  DEFAULT NULL COMMENT '模块',
  action     VARCHAR(20)  DEFAULT NULL COMMENT '动作：新增/修改/删除/重置口令/导出',
  detail     VARCHAR(500) DEFAULT NULL COMMENT '请求方法与路径',
  ip         VARCHAR(50)  DEFAULT NULL COMMENT '来源 IP',
  createtime DATETIME     DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (id),
  KEY idx_log_time (createtime),
  KEY idx_log_uname (uname)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '操作日志';

DELETE FROM role_permission
WHERE per_id IN (SELECT id FROM permission WHERE name = 'OperationLog');

DELETE FROM permission WHERE name = 'OperationLog';

INSERT INTO permission (pid, name, path, component, level, title)
VALUES (1, 'OperationLog', '/manage/log', 'OperationLog', 1, '操作日志');

INSERT INTO role_permission (roleName, per_id)
SELECT 'ROLE_1', id FROM permission WHERE name = 'OperationLog';

SELECT p.id, p.title, p.path FROM permission p WHERE p.name = 'OperationLog';
