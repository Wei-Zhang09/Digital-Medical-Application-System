-- 慧医数字医疗应用系统 · 演示账号口令重置
-- 作用：把管理员与全部医师账号口令统一为明文 123456（BCrypt 存储，勿写明文进 pwd 字段）
-- 用法：mysql -h127.0.0.1 -P3307 -uroot -p123456 --default-character-set=utf8mb4 bin_text < sql/reset_pwd.sql

UPDATE bin_text.account
SET pwd = '$2a$10$Ib.foSwKLbdana19proRoOUIyGmm3bVksgW3P364JNK4eJTsyT.Im'
WHERE uname IN ('admin_1', '王小明4125')
   OR utype = 'ROLE_2';

-- 核验：两条账号应为 ROLE_1 / ROLE_2，且 pwd 全部相同
SELECT uname, utype, LEFT(pwd, 7) AS pwd_prefix, CHAR_LENGTH(uname) AS name_len
FROM bin_text.account
WHERE uname IN ('admin_1', '王小明4125') OR utype = 'ROLE_2';
