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
- 看板：`hermes kanban --board huiyi-medical ls` —— T1–T4 已完成，T5 被 block（防守护进程抢活），T6–T14 排队
- **远端仓库已建并推送**：`https://github.com/Wei-Zhang09/Digital-Medical-Application-System`（分支 `main`，HEAD `6917ea5`，GitHub API 可见 90 个文件）
- **T1 完成**：Maven 用 `mvn.cmd` 正常（3.9.11 + Java 17）；MySQL 3307 实例已初始化；Node 22.13 就位
- **T2 完成**：`bin_text` 17 张表、16 账号、drug=6、permission=11、role_permission=18；口令已统一为 BCrypt(123456)
- **T3 完成**：后端 jar 可构建可启动；前端 serve 5s 编译、build 出 dist
- **T4 完成并实测**：`Msg`(20000/10000/10006)、`PageBean`、Account 四层实体、`JwtUtil`/`JwtFilter`、四个 Handler、`WebSecurityConfig`（放行 login/swagger/image、STATELESS、CORS、`@EnableGlobalMethodSecurity`）。实测：admin_1 → 20000/utype=1；王小明4125（UTF-8 提交）→ 20000/utype=2；错口令 → 10000；无 token → 10006
- 代码：`backend/`、`frontend/`、`sql/`、`scripts/`、`docs/`

### T4 落地时与计划的偏差（后续任务按此对齐）

| 计划写法 | 实际 | 原因 |
| --- | --- | --- |
| `domain/do/AccountDo.java` | `domain/dataobject/AccountDo.java` | `do` 是 Java 关键字，不能作包名 |
| `handler/AccessDeniedHandler.java` | `handler/AuthAccessDeniedHandler.java` | 与 Spring 同名接口冲突，类名加 Auth 前缀 |
| 登录返回 `utype:"1"` | `utype` 短码 "1"/"2" + `role` 完整 "ROLE_1"/"ROLE_2" 都给 | 前端判断用短码，`/api/permissions` 用完整角色名 |
| 未明确 | 四个 Handler 的 HTTP 状态码一律 200，业务码在响应体 | axios 只走成功分支，10006 在前端拦截器里判 |
| 未明确 | `server.servlet.encoding.force=true` | 否则非 UTF-8 客户端提交中文用户名查不到用户 |

- 回归脚本：`bash scripts/verify_t4.sh`（启动 jar → 跑 10 组用例 → 停应用），报告落 `.dev/verify-t4.txt`；`scripts/verify_t4_jwt.py` 校验 claim，`scripts/make_expired_jwt.py` 造过期 token
- 尚未验证：`@RolesAllowed/@PreAuthorize` 角色拦截（AccessDeniedHandler 已接好但无接口可触发，T6 加注解时验）；`/image/**` 只是放行，资源映射在 T6 上传功能里加


## 下一步

1. T5 菜单权限树 `/api/permissions`（管理员 9 项 / 医师 5 项）
2. T6 八个模块 CRUD + 药品图片上传
3. T7 前端请求层 + 登录 + 动态路由

## 待办/待确认

- 高德 Key 是否绑定「安全密钥」（D5 用地图时才知道）
- 远端仓库：`https://github.com/Wei-Zhang09/Digital-Medical-Application-System.git`（已确认存在且为空）

## 踩坑记录（重要）

- **看板守护进程会抢活**：`huiyi-medical` 看板的卡片一旦 ready，后台 dispatcher（60s 一轮）就会自动 spawn worker 去执行，而且它会直接写项目目录——T3 期间它创建了 jsconfig.json / favicon.ico / .gitkeep，还删掉了 `src/assets/logo.png`、`user.png`、`icon/icon.css`（已从 zip 补回）。**手工执行时：先 `hermes kanban claim <id>` 抢占，或把后续卡片 block 掉，完成一张再放开下一张。**
- 素材 zip 里有 3 个文件名在中央目录就已损坏（GBK 字节被当 cp437，`U+FFFD` 不可逆）：现以 `src/assets/legacy-01..03.png` 保存，内容完好，当前页面未引用。

## 环境副作用（本次会话造成，可还原）

- `hermes config`：`browser.headed=true`、`browser.cdp_url=http://127.0.0.1:9222`（为了让你在可见窗口登录 U+ 平台）。用完可 `hermes config set browser.headed false` 并清空 `cdp_url`。
- 另开了一个 Chrome for Testing 窗口（用户数据目录 `%LOCALAPPDATA%\Temp\eduplus-prof`），登录态在里面；关掉不影响项目。
