# HANDOVER · 慧医数字医疗应用系统

> 换模型/换会话时先读这个文件，再看 `.hermes/plans/2026-11-05_172117-huiyi-medical-dev.md`。

## 目标

个人（压缩版 5–7 个工作日）从零建成「慧医数字医疗应用系统」：Spring Boot 2.5.3 + Vue 2.6 + MySQL 8 + JWT 前后端分离，含数据面板与高德地图两项扩展；最终交付可运行系统 + 部署包 + 《项目分析与设计报告》docx + 答辩 PPT。

## 交付人信息

- 班级：软件工程232 ｜ 学号：2330142026 ｜ 姓名：张伟
- 打包命名：`软件工程232+2330142026+张伟.zip`
- 高德 Web端(JS API) Key：`caae559398c46473dd76566b9ae31f76`（已写入 `frontend/src/utils/amap.js`；若报 INVALID_USER_SCODE 需补安全密钥）

## 关键结论（已核实）

- 资料包 `E:\软工第10周` **只有素材，没有官方源码**：`后端素材.zip` 仅 sql + 图片 + 3 个配置类；`前端素材.zip` 仅 assets/style。所以源码全部自建。
- 官方 U+ 平台项目指导已抓取存档在 `docs/reference/`：5 阶段 / 40 任务 / 186 学时，含 16 张表的字段设计、29 个编码任务的具体步骤、评审与交付物要求（报告终版含测试报告、源码、答辩 PPT）。
- 本机环境与官方文档有差异，见下节；环境本身已修好。
- 报告模板 `项目分析与设计报告.docx` 是空白模板，示例内容还是「教材订购系统」，不能沿用。
- 种子数据 `drug.drug_img` 写死 `http://localhost:8080/image/...`，部署后旧图必裂，验收要用新上传的图。

## 本机环境实况（与官方文档的差异，重要）

| 项 | 官方/文档 | 本机实际 | 原因 |
| --- | --- | --- | --- |
| MySQL | 3306，服务 MySQL80 | **3307**，项目内实例，数据目录 `.dev/mysql/data`，`scripts/db.sh` 管理 | 系统服务 `MySQL09` 由管理员安装，普通用户不能启动、也读不到数据目录 |
| Node | 22.13 | `.dev/node22/PFiles64/nodejs`（MSI 管理安装解出），由 `scripts/env.sh` 前置 PATH | 不动系统 Node 26（其他项目在用） |
| Maven | `mvn` | `mvn.cmd`（`scripts/env.sh` 封装了 `mvn()` 函数） | MSYS 下 `bin/mvn` bash 包装报 classworlds 缺失 |
| JDK | 17 | 17.0.19 | 一致 |

用法：`source scripts/env.sh` 之后 `mvn` / `npm` / `mysql_bin` 都可用；`bash scripts/db.sh start|stop|status` 管数据库。

## 已确认的取舍

个人完成（不按 3 人组）；不走 CodeArts，本地 Git + 报告里补流程说明；高德 Key 由作者本人申请、地图做完整功能；报告先写 md，最后 python-docx 套模板导出 docx。

## 当前状态（2026-11-05）

- 计划：`.hermes/plans/2026-11-05_172117-huiyi-medical-dev.md`（14 个任务、7 天排期、DoD）
- 看板：`hermes kanban --board huiyi-medical ls` —— T1/T2 已完成，T3 进行中，T4–T14 排队
- **T1 完成**：Maven 用 `mvn.cmd` 正常（3.9.11 + Java 17）；MySQL 3307 实例已初始化；Node 22.13 就位
- **T2 完成**：`bin_text` 17 张表、16 账号、drug=6、permission=11、role_permission=18；`sql/reset_pwd.sql` 已把 admin_1 与全部 ROLE_2 口令重置为 BCrypt(123456)
- **T3 进行中**：后端骨架可跑——`mvn clean package` 产出 `medicai-1.0-SNAPSHOT.jar`，`java -jar` 启动成功（Spring Boot 2.5.3，5.37s，Druid 连库成功，Swagger 已映射；此时全站 401 属正常，T4 加 WebSecurityConfig 后放行）。前端骨架文件已写（package.json / vue.config.js / main.js / router / store / Login 占位 / amap.js，assets+style 已合入），`npm install` 在跑
- 代码：`backend/`、`frontend/`、`sql/`、`scripts/`、`docs/`

## 下一步

1. 等 `npm install` 完 → `npm run serve` 起 9092 验证骨架 → T3 收尾
2. T4 公共层 + JWT + Spring Security（后端第一个真功能）
3. T5 菜单权限树 → T6 八个模块 CRUD

## 待办/待确认

- 高德 Key 是否绑定「安全密钥」（D5 用地图时才知道）
- 远端仓库：`https://github.com/Wei-Zhang09/Digital-Medical-Application-System.git`（已确认存在且为空）

## 踩坑记录（重要）

- **看板守护进程会抢活**：`huiyi-medical` 看板的卡片一旦 ready，后台 dispatcher（60s 一轮）就会自动 spawn worker 去执行，而且它会直接写项目目录——T3 期间它创建了 jsconfig.json / favicon.ico / .gitkeep，还删掉了 `src/assets/logo.png`、`user.png`、`icon/icon.css`（已从 zip 补回）。**手工执行时：先 `hermes kanban claim <id>` 抢占，或把后续卡片 block 掉，完成一张再放开下一张。**
- 素材 zip 里有 3 个文件名在中央目录就已损坏（GBK 字节被当 cp437，`U+FFFD` 不可逆）：现以 `src/assets/legacy-01..03.png` 保存，内容完好，当前页面未引用。

## 环境副作用（本次会话造成，可还原）

- `hermes config`：`browser.headed=true`、`browser.cdp_url=http://127.0.0.1:9222`（为了让你在可见窗口登录 U+ 平台）。用完可 `hermes config set browser.headed false` 并清空 `cdp_url`。
- 另开了一个 Chrome for Testing 窗口（用户数据目录 `%LOCALAPPDATA%\Temp\eduplus-prof`），登录态在里面；关掉不影响项目。
