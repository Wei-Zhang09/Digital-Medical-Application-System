# 慧医数字医疗应用系统 · 开发计划（个人压缩版 · 5–7 个工作日）

> **给 Hermes：** 按本计划逐任务执行，每个任务完成后跑「验证」里的命令并把输出贴回报告素材区。看板：`hermes kanban --board huiyi-medical`。

**目标：** 从零建成一套可运行、可演示、可部署的「慧医数字医疗应用系统」（Spring Boot 2.5.3 + Vue 2.6 + MySQL 8 + JWT 前后端分离），并产出可提交的《项目分析与设计报告》docx、源代码、部署包与答辩材料。

**架构：** 后端按 `controller → service → mapper(XML) → domain/model` 分层，统一返回体 `Msg`，Spring Security + JWT 做认证、`@RolesAllowed` + `permission/role_permission` 做两级授权；前端 Vue 2.6 + Element UI，`routeParse` 按 `/api/permissions` 动态挂载菜单；Nginx 托管 dist 并反代 `/api/`、`/image/`。

**技术栈（锁定，与官方任务书一致，不得升版）：** JDK 17 / Spring Boot 2.5.3 / mybatis-spring-boot 2.2.0 / druid 1.2.5 / mysql-connector 8.0.20 / PageHelper / Spring Security / JWT(jjwt) / Swagger2；Vue 2.6.11 / Element UI 2.4.5 / axios 0.21 / vue-router 3 / Vuex 3 / ECharts 4.9 / 高德 JS API 1.4；MySQL 8.0.x；Nginx 1.20+。

**资料基线（本机 E:\软工第10周）：** `项目素材/bin_text.sql`（17 张表 + 种子数据）、`后端素材.zip`（JacksonConfig/SwaggerConfig/WebConfig + 图片素材）、`前端素材.zip`（assets/style/less）、`项目分析与设计报告.docx`（空白模板）、`师资培训课件/*`（讲师手册、部署说明、00–04 脚本）、`开发环境/*`（JDK17、Node22.13、MySQL8、IDEA、VSCode）。

**U+ 平台官方指导已抓取存档：** `docs/reference/00-项目介绍.md`、`01-项目指导-全量任务.md`（5 阶段 / 40 任务 / 186 学时）、`01-项目指导-任务索引.md`。开发时以官方 40 任务为准，本计划把它压到 7 天。

**结构参考（只读，不抄）：** GitHub `hongmengchen/hy-health`（Mulan PSL-2.0，295 文件）是同一案例的公开实现，模块与官方任务书一一对应：11 个 controller（City/Company/CompanyPolicy/Doctor/Drug/Material/MedicalPolicy/Permission/Sale/Dashboard/FileUpload）、11 个 views（含 Login 与 8 个管理页）、`sql/hy-health.sql` + `sql/new-sale.sql`（地图扩展建表）。**用途限于对照分层与接口命名**——本项目要作为个人作业提交，代码必须自己写，参考实现不进仓库、不进报告。

**已确认的取舍：** 个人完成（不按 3 人组分工写报告，组织架构章节按"个人承担多角色"表述）；不走 CodeArts，用本地 Git，报告第 2 章补 CodeArts 流程说明；高德 Key 由作者本人申请，地图按完整功能做；报告先写 md，最后用 python-docx 按模板导出 docx。

---

## 0. 现状与阻塞项（开工前必须先解决）

| # | 问题 | 证据 | 处理 |
| --- | --- | --- | --- |
| B1 | 官方源码包缺失，资料包只有「素材」 | `项目素材/后端素材.zip` 仅 23 项：sql + 图片 + 3 个配置类，无 `pom.xml`/`src`；`前端素材.zip` 仅 assets/style | **按官方 40 任务自建源码**，不找现成包 |
| B2 | Maven 不可用 | `mvn -version` 报 `ClassNotFoundException: org.codehaus.plexus.classworlds.launcher.Launcher`，`MAVEN_HOME=D:\Work_app\apache-maven-3.9.11`，`M2_HOME` 为空 | **已解决**：安装本身完好，是 MSYS 下的 `bin/mvn`（bash 包装）跑不起来；改用 `bin/mvn.cmd`（`scripts/env.sh` 里已封装 `mvn()`）→ Maven 3.9.11 + Java 17 正常 |
| B3 | MySQL 未运行 | 服务名 `MySQL09`，状态 `Stopped`，且普通用户无权启动/读取其数据目录 | **已解决（换方案）**：项目内独立实例，端口 **3307**，数据目录 `.dev/mysql/data`，`scripts/db.sh start\|stop\|status` 管理 |
| B4 | Node 版本过高 | 本机 `node v26.7.0`，官方指定 22.13；vue-cli 4/webpack 4 在 Node 17+ 需 `--openssl-legacy-provider` | **已解决（不动系统）**：`msiexec /a` 把官方 MSI 解到 `.dev/node22/PFiles64/nodejs`，`scripts/env.sh` 前置 PATH → `node -v` = v22.13.0 / npm 10.9.2 |
| B5 | 种子图片 URL 写死 `localhost:8080` | `bin_text.sql` 中 `drug.drug_img` 全为 `http://localhost:8080/image/...` | 换机/部署后旧图必裂；验收统一用**新上传**的图；部署阶段批量替换主机名 |
| B6 | 扩展 `sale.sql` 不在资料包 | 只有 `bin_text.sql` | 自写 `sql/sale_ext.sql`：给 `sale` 加 `lng/lat/address` 并造示例点 |

---

## 1. 目录与仓库约定

```
D:\HermesWorkSpace\Digital Medical Application System\
├─ backend\                     # Spring Boot 工程（artifactId: medicai）
│  ├─ pom.xml
│  └─ src\main\java\com\qst\medical\
│     ├─ MedicalApplication.java
│     ├─ config\               # WebSecurityConfig / SwaggerConfig / JacksonConfig / WebConfig / CorsConfig
│     ├─ security\             # JwtUtil / JwtFilter / 4 个 Handler
│     ├─ common\               # Msg / PageBean / 常量
│     ├─ domain\               # Entity(表) → DO(库操作) → DTO(前端入参) → VO(出参) 四层
│     ├─ mapper\               # 接口 + resources\mapper\*.xml
│     ├─ service\              # 接口 + impl
│     └─ controller\
│  └─ src\main\resources\application.properties
├─ frontend\                    # 目录名固定 medical-managerment-system（少一个 a，别改）
│  ├─ package.json / vue.config.js
│  └─ src\{api,assets,components,layout,router,store,style,utils,views}
├─ sql\bin_text.sql  sql\sale_ext.sql  sql\reset_pwd.sql
├─ docs\reference\              # U+ 官方指导存档（已生成）
├─ docs\report\                 # 报告 md 分章 + 导出脚本 + 最终 docx
├─ scripts\                     # 本地一键：db-init / build / run / verify
└─ .hermes\plans\               # 本计划
```

Git：本地仓库，分支 `main` + 每日 `day-N` 分支；每完成一个任务一次提交（`feat(backend): 药品分页查询`）。报告第 2 章据此补 CodeArts 流程说明（不实际注册）。

---

## 2. 七天排期总览

| 天 | 主线 | 对应官方任务 | 交付物 | 验收 |
| --- | --- | --- | --- | --- |
| D0（半天） | 环境修复 + 工程骨架 + 库导入 | 环境搭建 任务3/4（本地版） | 能 `mvn package`、`npm run serve` 的空工程 | 两个 hello 接口/页面通 |
| D1 | 认证与权限闭环 | 后端 任务1、任务19 | 登录发 JWT、`/api/permissions` 菜单树 | 两个账号登录、菜单差异正确 |
| D2 | 全部主数据后端 CRUD + 药品上传 | 后端 任务3–18 | 8 个模块 REST 接口 + Swagger | Swagger 全接口 200，图片可访问 |
| D3 | 前端骨架 + 登录 + 动态路由 + 药品页 | 前端 任务20/21/25 | 登录页、Layout、药品管理页 | 页面登录成功、药品 CRUD 全通 |
| D4 | 其余 7 个前端模块 | 前端 任务22–24、26–29 | 8 个管理页 | 每页增删改查跑通 |
| D5 | 扩展：数据面板 + 高德地图 | 扩展 任务（4h+2h） | `/api/dashboard` + 首页图表 + 地图开关 | 图表有数、地图打点/改点可用 |
| D6 | 部署 + 测试 | 部署/云测（选做→必做） | jar + dist + Nginx 站点 + 用例表 | 验收五下全过、用例执行记录 |
| D7 | 报告 + 答辩 + 总结 | 验收 任务 | 报告 docx + PPT + 源码包 | 报告 9 章齐、答辩能跑 |

> 每天结束提交 + 在 `docs/report/素材-日志.md` 记三行：今天做完什么、卡在哪、明天第一件事。

---

## 3. 任务分解

### Task 1（D0-a）修环境：Maven / MySQL / Node

**目标：** 三条命令都正常，库能连上。

**步骤：**
1. 修 Maven：`ls D:/Work_app/apache-maven-3.9.11/lib` 应含 `plexus-classworlds-*.jar`；缺则重下 apache-maven-3.9.11 覆盖，并设 `MAVEN_HOME` + `Path` 加 `%MAVEN_HOME%\bin`。
2. 启动 MySQL：`powershell -c "Set-Service MySQL09 -StartupType Automatic; Start-Service MySQL09"`。
3. 装 Node 22.13：运行 `E:\软工第10周\开发环境\node-v22.13.0-x64.msi`，确认 `node -v` = `v22.13.0`。

**验证：**
```bash
mvn -version          # 期望 Maven 3.9.x / Java 17
node -v               # 期望 v22.13.0
mysql -uroot -p123456 -e "SELECT VERSION();"   # 期望 8.0.x
```

---

### Task 2（D0-b）建库导入 + 口令重置

**Files:** `sql/bin_text.sql`（从 `E:\软工第10周\项目素材\` 复制）、`sql/reset_pwd.sql`

**步骤：**
1. `mysql -uroot -p123456 --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS bin_text DEFAULT CHARSET utf8mb4;"`
2. 中文路径易失败：先 `cp` 到 `$TMPDIR/bin_text.sql` 再 `mysql ... bin_text < $TMPDIR/bin_text.sql`。
3. `reset_pwd.sql`：把 `admin_1` 与 `utype='ROLE_2'` 的 `pwd` 统一改为 BCrypt(`123456`) 密文 `$2a$10$Ib.foSwKLbdana19proRoOUIyGmm3bVksgW3P364JNK4eJTsyT.Im`。

**验证：**
```bash
mysql -uroot -p123456 -N -e "SELECT uname,utype FROM bin_text.account WHERE uname IN ('admin_1','王小明4125'); SELECT COUNT(*) FROM bin_text.drug; SELECT COUNT(*) FROM bin_text.permission;"
# 期望：两行账号、drug>0、permission>0
```

---

### Task 3（D0-c）后端骨架 + 前端骨架

**Files:** `backend/pom.xml`、`backend/src/main/resources/application.properties`、`frontend/package.json`、`frontend/vue.config.js`

**步骤：**
1. 后端：按官方 pom（Spring Boot 2.5.3 parent，`groupId=com.qst`、`artifactId=medicai`，`maven.compiler.source/target=17`；依赖：web、mybatis-spring-boot 2.2.0（排除 HikariCP）、druid 1.2.5、mysql-connector 8.0.20、spring-boot-starter-security、jjwt、swagger2+swagger-ui、pagehelper、devtools、starter-test、lombok）。
2. `application.properties`：`server.port=8080`、`spring.datasource.url=jdbc:mysql://127.0.0.1:3306/bin_text?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai`、`username=root`、`password=123456`、`spring.servlet.multipart.max-file-size=1MB`、`upload.path=uploads`。
3. 前端：`vue create medical-managerment-system`（选 Vue2），按官方 `package.json` 锁定依赖版本；`vue.config.js` 设 `devServer.port=9092`、`publicPath: './'`；把 `前端素材.zip` 的 `assets/`、`style/` 合入 `src/`。
4. `git init` + `.gitignore`（target/node_modules/uploads/dist）。

**验证：**
```bash
cd backend && mvn -q clean package -DskipTests && ls target/*.jar
cd frontend && npm install && npm run serve   # 期望 http://localhost:9092 打开
```

---

### Task 4（D1-a）公共层 + 用户模型 + JWT + Security

**Files:** `common/Msg.java`、`common/PageBean.java`、`domain/entity/Account.java`、`domain/do/AccountDo.java`、`domain/dto/AccountDto.java`、`domain/model/AccountModel.java`、`mapper/AccountMapper.java` + `resources/mapper/AccountMapper.xml`、`security/JwtUtil.java`、`security/JwtFilter.java`、`security/handler/{AuthEntryPoint,AuthFailHandler,AuthSuccessHandler,AccessDeniedHandler}.java`、`config/WebSecurityConfig.java`

**要点（照官方任务 1 的 11 步）：** 分页公共类 → 实体四层 → `AccountDto implements UserDetails` → mapper 查用户与角色 → `JwtUtil.getJwtToken(id,uname,role)`（HS256，claim 带 id/uname/role）→ `JwtFilter` 置于 `UsernamePasswordAuthenticationFilter` 之前 → 四个 Handler 统一返回 `Msg` → `WebSecurityConfig`：放行 `/api/login`、`/v2/api-docs`、`/swagger-*`、`/swagger-ui.html`、`/webjars/**`、`/image/**`，其余 `authenticated()`，`STATELESS`，开 CORS，关 CSRF。

**返回码约定：** `20000` 成功、`10000` 业务失败、`10006` 未登录/token 失效。

**验证：**
```bash
curl.exe --max-time 8 -s -X POST "http://127.0.0.1:8080/api/login" -d "username=admin_1&password=123456"
# 期望 {"code":20000,...,"utype":"1"}；换成 王小明4125 得 "utype":"2"
curl.exe --max-time 8 -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8080/swagger-ui.html/
```

---

### Task 5（D1-b）菜单权限树 `/api/permissions`

**Files:** `domain/model/{PermissionModel,PermissionMetaModel}.java`、`mapper/PermissionMapper.java` + XML、`service/PermissionService.java`、`controller/PermissionController.java`

**要点（官方任务 19）：** 按 `role_permission.roleName` 取 `permission` 行 → 按 `pid` 递归组装树（`getAllPermission(roleName)` + `selectChildren`）→ 返回 `{path,name,component,meta{title,icon}}` 结构；非 `ROLE_1` 时把标题里的"管理"替换为"查询"。

**验证：** 管理员 token 取到的菜单 9 项（首页、医药公司、销售地点、城市、药品、医保政策、公司政策、医生、必备材料），医师 token 取到 5 项。

---

### Task 6（D2）八类主数据后端 CRUD + 药品上传

**Files（每模块一套，路径示例以药品为准）：**
`domain/entity/Drug.java`、`domain/model/DrugModel.java`、`domain/param/DrugParam.java`、`mapper/DrugMapper.java`、`resources/mapper/DurgMapper.xml`（**文件名保持官方拼写**）、`service/DrugService.java`、`controller/DrugController.java`

**模块与接口（统一 `/api/xxx`）：**

| 模块 | 查询 | 增 | 改 | 删 | 角色 |
| --- | --- | --- | --- | --- | --- |
| 医师 doctor | `GET /api/doctors/{pn}/{size}?name=` | POST | PUT | DELETE + `PUT /api/doctors/pwd/{id}` 重置密码 | 查 1/2；改删仅 1 |
| 医药公司 drugcompany | 同上 | POST | PUT | DELETE | 1 |
| 公司政策 companyPolicy | `GET ?title=` | POST | PUT | DELETE | 1 |
| 城市 city | `GET /api/cities` | POST | — | DELETE | 1 |
| 医保政策 medicalPolicy | `GET ?title=` | POST | PUT | DELETE | 1 |
| 销售地点 sale | `GET ?name=` | POST | PUT | DELETE | 1 |
| 药品 drug | `GET /api/drugs/{pn}/{size}?name=` | POST | PUT | DELETE | 增 1/2；改删仅 1 |
| 必备材料 material | `GET ?title=` | POST | PUT | DELETE | 1 |

**关键实现点：**
1. 分页统一用 PageHelper，返回 `Msg.data.{xxxPageInfo}`（`list,total,pages,pageNum,pageSize`）。
2. 药品新增/修改要处理 `drug_sale` 关联（先删旧关系再插新关系）；删除药品同时删 `drug_sale`。
3. 图片上传：`POST /api/upload`（multipart）→ 存 `uploads/`（jar 工作目录下，启动前 `mkdir uploads`）→ 返回 `/image/xxx.jpg`；`WebConfig` 把 `/image/**` 映射到文件目录。
4. 统一异常处理 `@RestControllerAdvice`，把 `AccessDeniedException` 转 `10000`。

**验证：** Swagger 逐接口点通；`curl` 带 token 查询 `GET /api/drugs/1/5` 返回 `drugPageInfo`；上传一张 <1MB 图，`curl -I http://127.0.0.1:8080/image/<文件名>` 返回 200。

---

### Task 7（D3）前端骨架：请求层 + 登录 + 动态路由

**Files:** `src/utils/request.js`、`src/utils/app.js`、`src/api/Login.js`、`src/api/permission.js`、`src/views/Login.vue`、`src/router/index.js`、`src/router/routeParse.js`、`src/layout/**`、`src/store/modules/user.js`、`src/views/Home/index.vue`

**要点：** `baseURL = "http://localhost:8080/api"`（开发态）；请求拦截加 `Authorization: Bearer <token>`；响应拦截 `code!==20000` 用 `app.js` 统一提示，`10006` 清本地状态并回登录页；登录成功存 token + 角色，拉 `/api/permissions` 由 `routeParse` 动态 `addRoutes`；路由用 hash 模式（地址带 `#`）。

**验证：** `admin_1` 登录后左侧 9 个菜单、`王小明4125` 登录后 5 个；刷新不丢登录；未登录直访内页回登录页。

---

### Task 8（D3–D4）九个业务页面

**Files（`src/views/` 下各一目录 + `src/api/admin/*.js` + `src/store/modules/*.js`）：**
`DrugManage`、`DrugCompanyManage`、`CompanyPolicyManage`、`CityManage`、`MedicalPolicyManage`、`SaleManage`、`DoctorManage`、`MaterialManage`、`Home`

**统一模板：** 顶部搜索（关键字 + 查询/重置）→ 表格（分页）→ 新增/编辑弹窗（表单校验）→ 删除二次确认 → 操作反馈提示。药品页额外有 `el-upload` 上传与图片预览列；医生页额外有"重置密码"。

**验证（每页都过一遍）：** 新增一条 → 列表出现 → 修改生效 → 删除后消失 → 翻页正常 → 医师账号进同一页无新增/编辑/删除按钮。

---

### Task 9（D5）扩展一：数据面板

**Files:** 后端 `controller/DashboardController.java`（`GET /api/dashboard`，角色 1/2）、`mapper/DashboardMapper.java` + XML；前端 `src/api/admin/dashboardApi.js`、`src/views/Home/index.vue`

**内容：** 医师/公司/药品/药店四个计数、医师职称柱状图（`doctor.level_id` join `doctor_level`）、科室饼图（`treat_type`）、最新资讯列表（`material` 或 `medical_policy` 取最新 5 条）。图表用 ECharts 4.9。

**验证：** 首页四个数字与 `SELECT COUNT(*)` 一致；两张图有数据；无新增菜单项。

---

### Task 10（D5）扩展二：销售地点地图

**Files:** `sql/sale_ext.sql`、后端 `sale` 实体/Param/XML 增 `lng/lat/address`、前端 `src/views/SaleManage/index.vue` + `src/utils/amap.js`

**步骤：**
1. `ALTER TABLE sale ADD COLUMN lng double, ADD COLUMN lat double, ADD COLUMN address varchar(255);` 并插入 3 条青岛示例点。
2. 后端增删改查带上三个字段。
3. 前端：列表页加"地图展示"开关（`el-switch`）；开→渲染高德地图并打点；点"新增"后再点地图落点（回填经纬度）；点已有标记弹修改框。
4. Key：作者在 [高德开放平台](https://console.amap.com/dev/key/app) 申请 **Web端(JS API)** Key，配置到 `src/utils/amap.js` 的 `key` 与 `securityJsCode`（新版需安全密钥）。

**验证：** 开关切换列表/地图；新增点后刷新仍在；点标记能改名称与电话；断网时页面不白屏（给降级提示）。

---

### Task 11（D6）部署：jar + dist + Nginx

**步骤（顺序不能反）：**
1. `cd backend && mvn clean package -DskipTests` → `target/medicai-1.0-SNAPSHOT.jar`。
2. 改 `frontend/src/utils/request.js` 的 `baseURL` 为 `"/api"` → `npm run build` → `dist/`（构建后**改回**完整地址以便继续开发）。
3. 后端启动：`cd backend/target && mkdir -p uploads && java -jar medicai-1.0-SNAPSHOT.jar`。
4. Nginx（本机 `C:\nginx`，若未装则从 nginx.org 下 1.20.x zip）：
```nginx
server {
    listen 80; server_name _;
    root html/medical; index index.html;
    location /api/   { proxy_pass http://127.0.0.1:8080/api/;   proxy_set_header Host $host; }
    location /image/ { proxy_pass http://127.0.0.1:8080/image/; proxy_set_header Host $host; }
}
```
5. `copy dist/* → C:\nginx\html\medical`；`cd C:\nginx && nginx.exe -t && nginx.exe`。

**验收五下：** ① 用 `http://127.0.0.1/` 打开登录页（地址里没有 9092）② `admin_1` 登录菜单完整 ③ 药品列表有种子数据 ④ 新上传的图能显示 ⑤ 医师账号菜单变少。

**注意：** `proxy_pass` 末尾的 `/api/` 必须保留；种子旧图仍指向 `localhost:8080`，验收用新图；若要旧图可用，执行 `UPDATE drug SET drug_img=REPLACE(drug_img,'http://localhost:8080','')`。

---

### Task 12（D6）测试：用例 + 执行记录

**Files:** `docs/report/测试用例.md`、`docs/report/测试执行记录.md`、`backend/src/test/java/...`（关键 service 单测）

**内容：**
1. 单元测试：`DrugServiceTest`（分页、增删改、关联表清理）、`JwtUtilTest`（签发/校验/过期）、`PermissionServiceTest`（树组装）。`mvn test` 全绿。
2. 功能用例 ≥ 20 条：登录（正确/错误/无 token/过期 token）、权限（医师访问改删接口应 403）、8 个模块各 1 条 CRUD、上传（>1MB 应拒）、分页边界、SQL 注入样例。
3. 接口用例：用 Postman 集合导出 `docs/report/postman-collection.json`，逐条记录实际返回码。
4. 缺陷记录：发现即登记（现象/复现/原因/修复/回归结论）。

**验证：** `mvn test` 输出 `Tests run: N, Failures: 0`；用例表每条有"通过/失败"结论与截图编号。

---

### Task 13（D7）报告：按模板九章成文 → 导出 docx

**模板章节映射（`项目分析与设计报告.docx` → 本项目内容）：**

| 章 | 模板要求 | 本项目素材来源 |
| --- | --- | --- |
| 1 项目立项 | 背景/客户与最终用户/产品范围 | `docs/reference/00-项目介绍.md` 1.1–1.3；角色：管理员、医师 |
| 2 项目启动 | 组织结构/进度计划 | 个人承担全部角色（组长=本人）；进度用 D0–D7 排期表 + 看板截图替代 DevCloud 截图；补 CodeArts 流程说明段落 |
| 3 需求分析 | 业务流程/功能分类/功能 A、B/非功能 | `01-项目指导` 任务1「项目需求描述」：8 大模块 30 项子功能、性能与质量需求原文 |
| 4 概要设计 | 体系结构/功能结构/运行环境/接口/部署 | 讲师手册 + 本文档第 1、11 节；体系结构图用前后端分离三层图 |
| 5 详细设计 | 模块汇总 + 模块设计（接口/数据结构/流程） | 按 8 模块写；每个模块贴 service 方法签名与处理流程（如药品：查→增→关联 drug_sale→删） |
| 6 界面设计 | 界面清单/关系图/原型 | 登录页 + 9 个管理页截图；关系图：登录 → 动态菜单 → 各模块 |
| 7 数据库设计 | 环境/ERD/表汇总/逐表字段/安全性 | `01-项目指导` 任务3 的 16 张表字段表（已存档）+ `bin_text.sql` 实测；ERD 用 drawio 导出 PNG |
| 8 编码与单元测试 | 代码检查/单测结果 | Task 12 的 `mvn test` 输出 + 代码规范检查（IDEA Inspect Code 截图） |
| 9 软件测试 | 计划/用例/执行/总结 | Task 12 三份文档汇总 |

**流程：** 每章写成 `docs/report/0X-*.md` → `scripts/export_report.py`（python-docx）按模板样式生成 `docs/report/慧医数字医疗应用系统-项目分析与设计报告.docx` → 人工核对占位符无残留。

**验证：** docx 打开无"【…】"残留、无"教材订购"字样、图片全部内嵌、页码目录正常。

---

### Task 14（D7）答辩与总结

1. `docs/report/答辩提纲.md`：演示脚本（管理员全流程 8 分钟 → 换医师对比 2 分钟 → 讲药品链路 5 分钟 → 部署验收 3 分钟）+ 三个必答口播（权限在服务端、列表套路一致、图片地图是加分项）。
2. 答辩 PPT（15–20 页）：项目背景、架构、库表、核心链路、扩展、部署、测试、总结。
3. `项目总结报告.md`：过程复盘、问题与解决（Maven/Node/中文路径/图片 URL 等真实踩坑）、心得。
4. 打包：`专业+班级+学号+姓名.zip` = 报告 docx + 源码 + sql + 答辩 PPT（+ 视频）。

---

## 4. 风险与对策

| 风险 | 影响 | 对策 |
| --- | --- | --- |
| 无官方源码，全量自建 | 工期紧（官方 158h 开发量） | 严格"先一个模块跑通再复制"：药品 → 材料 → 公司 → 政策 → 城市 → 医生；页面用同一套模板改字段 |
| Node 26 与 vue-cli 4 不兼容 | 前端起不来 | 装 Node 22.13；若仍报 `ERR_OSSL_EVP_UNSUPPORTED`，`set NODE_OPTIONS=--openssl-legacy-provider` |
| Maven 环境损坏 | 无法打包 | Task 1 先修；兜底用 IDEA 内置 Maven 或 `mvnw` |
| 中文路径/编码 | 导入 SQL 失败、乱码 | 库 `utf8mb4`、JDBC `characterEncoding=utf8`、导入走英文临时路径 |
| 高德 Key 域名限制 | 地图空白 | 控制台把 `localhost`/`127.0.0.1` 加白名单；或先用 `securityJsCode` 代理校验；实在不行降级为"静态点列表"并如实说明 |
| 报告 9 章工作量大 | 拖到最后崩 | 每完成一天就回填对应章节，D7 只做整合与导出 |
| 个人完成与模板"组织结构"冲突 | 报告失真 | 按"一人多角色"如实填写，并在总结里说明分工压缩原因 |

## 5. 完成定义（DoD）

- [ ] `mvn clean package` 与 `npm run build` 均成功，产物可启动
- [ ] 两个账号登录成功，菜单差异与官方一致（9 / 5）
- [ ] 8 个模块 CRUD + 图片上传 + 菜单权限全部可用
- [ ] 数据面板与地图两项扩展可操作
- [ ] Nginx 80 端口验收五下全过
- [ ] `mvn test` 全绿；功能用例 ≥20 条且有执行结论
- [ ] 报告 docx 九章齐、无占位符、含测试报告
- [ ] 源码 + sql + 报告 + PPT 打包完成

## 6. 待办确认（不阻塞开工）

1. 班级/学号/姓名（打包命名与报告封面用）——随时给。
2. 高德 Key 申请时间——D5 前给即可。
3. 是否需要答辩视频录制。
