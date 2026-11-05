-- 把药品图片地址从写死的 http://localhost:8080/image/xxx 改成相对路径 /image/xxx
--
-- 为什么：种子里全是绝对地址，换机器或部署到 Nginx 后浏览器会去访问访问者自己的 8080，
-- 图片必然裂开。改成相对路径后，开发态由 .env.development 拼回 8080，
-- 生产态由 Nginx 反代 /image/，两边都不用改代码。
--
-- 用法：mysql ... bin_text < sql/image_url_relative.sql

UPDATE drug
SET drug_img = REPLACE(drug_img, 'http://localhost:8080/image/', '/image/')
WHERE drug_img LIKE 'http://localhost:8080/image/%';

SELECT drug_id, drug_name, drug_img FROM drug ORDER BY drug_id LIMIT 6;
