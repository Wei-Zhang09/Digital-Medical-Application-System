# 用 IDEA + VS Code 启动项目

> 后端在 IDEA 里跑，前端在 VS Code 里跑，数据库用项目自带的 3307 实例。
> 项目根目录：`D:\HermesWorkSpace\Digital Medical Application System`

## 0. 数据库：两种选法，二选一

### 方案 A（推荐）：用你本机的 MySQL09 服务，端口 3306

系统服务名就是 `MySQL09`，开机自启。首次只需要把库导进去一次：

1. 确认服务在跑：`services.msc` 里看 MySQL09，或管理员命令行 `net start MySQL09`
2. **双击 `scripts\导入数据库到本机MySQL.bat`**，按提示输入一次 root 密码
   （脚本会依次建库、导入 `digital_medical_application_system.sql`、重置口令、加地图字段、改图片路径）
3. 之后不用再管它，IDEA 直接启动后端即可

`application.properties` 默认就指向 `127.0.0.1:3306`，不需要改配置。

### 方案 B：用项目自带的 3307 实例（隔离，不碰你的 MySQL09）

| 项 | 值 |
| --- | --- |
| 地址 | `127.0.0.1:3307` |
| 库名 | `digital_medical_application_system` |
| 账号 | `root` / `123456` |
| 数据目录 | `.dev\mysql\data` |

**双击 `scripts\启动数据库.bat`**（停止用 `scripts\停止数据库.bat`）。
用这个方案时，启动后端要带上环境变量 `DB_PORT=3307`：

- IDEA：`Run → Edit Configurations → Environment variables` 加 `DB_PORT=3307`
- 命令行：`DB_PORT=3307 bash scripts/dev.sh`

> 两个库互不影响。数据都已经导好（17 张表 + 种子数据），不需要重复导入。

## 1. 后端：IDEA

### 1.1 打开

`File → Open` → 选 **`backend` 目录**（里面是 pom.xml）→ Trust Project。
等右下角 Maven 依赖下载完（首次约 2–5 分钟，本机已配阿里云镜像）。

### 1.2 配 JDK 17

`File → Project Structure`（Ctrl+Alt+Shift+S）：

- **Project**：SDK 选 `17`（`C:\Program Files\Java\jdk-17.0.19`），Language level 选 `17`
- **Modules**：确认 `medicai` 的 SDK 也是 17

> 项目是 Spring Boot 2.5.3，**不要用 JDK 21/24**，会有兼容问题。

### 1.3 Maven（可选）

`Settings → Build, Execution, Deployment → Build Tools → Maven`：

- Maven home path：用 IDEA 自带的（Bundled 3.9.x）就够，也可以填 `D:\Work_app\apache-maven-3.9.11`
- 不需要改 `mvn` 命令行的 classworlds 问题，IDEA 不走那个 bash 包装脚本

### 1.4 启动

打开 `src/main/java/com/qst/medical/MedicalApplication.java`，点左边绿色三角 → Run。

**建议改一下工作目录**（`Run → Edit Configurations`）：

| 项 | 值 |
| --- | --- |
| Main class | `com.qst.medical.MedicalApplication` |
| JRE | 17 |
| Working directory | `D:\HermesWorkSpace\Digital Medical Application System\backend\target` |

为什么：`application.properties` 里 `upload.path=uploads`，是相对工作目录的。
设成 `target` 就跟命令行 `java -jar` 完全一致，图片走 `backend\target\uploads`。
（不设也能跑：默认工作目录是 `backend`，种子图我已经同时放到 `backend\uploads` 了。）

控制台出现 `Started MedicalApplication in x seconds` 就是成功。

### 1.5 验证

浏览器打开 **http://127.0.0.1:8080/swagger-ui.html/**（末尾斜杠必须写）。

### 1.6 连数据库看表（可选）

`View → Tool Windows → Database` → `+` → Data Source → MySQL：

- Host `127.0.0.1`，Port `3307`，User `root`，Password `123456`，Database `digital_medical_application_system`
- 首次会提示下载驱动，点 Download

## 2. 前端：VS Code

### 2.1 打开

`File → Open Folder` → 选 **`frontend` 目录**。

### 2.2 终端里指定 Node 22

系统装的是 Node 26，本项目要用项目内的 22.13。在 VS Code 终端（Ctrl+`，PowerShell）执行：

```powershell
$env:Path = "D:\HermesWorkSpace\Digital Medical Application System\.dev\node22\PFiles64\nodejs;" + $env:Path
node -v      # 必须显示 v22.13.0
```

想每次自动生效，可以在 `frontend\.vscode\settings.json` 里加：

```json
{
  "terminal.integrated.env.windows": {
    "PATH": "D:\\HermesWorkSpace\\Digital Medical Application System\\.dev\\node22\\PFiles64\\nodejs;${env:PATH}"
  }
}
```

### 2.3 安装依赖 + 启动

```powershell
npm install      # 只有第一次需要（约 5 分钟，已配 npmmirror 镜像）
npm run serve
```

看到 `App running at: http://localhost:9092` 后打开：

```text
http://localhost:9092/#/user/login
```

**账号**：管理员 `admin_1` / `123456`　医师 `王小明4125` / `123456`

### 2.4 前端配置在哪

| 文件 | 作用 |
| --- | --- |
| `.env.development` | 开发态接口地址 `http://localhost:8080/api`、图片前缀 `http://localhost:8080` |
| `.env.production` | 打包时用 `/api`（交给 Nginx 反代），**不要在开发态改这个** |
| `vue.config.js` | 开发端口 9092、publicPath `./` |
| `src/utils/request.js` | axios 实例、token 注入、10006 处理 |

改 `.env.*` 后必须**重启** `npm run serve` 才生效。

## 3. 一次性准备清单（换新电脑时）

1. 装 JDK 17、Node 22（项目内已有 `.dev\node22`，可直接复用整个 `.dev` 目录）
2. 数据库：`bash scripts/db.sh start`，若数据目录是空的则先导入 `sql/digital_medical_application_system.sql`
3. 种子图片：`bash scripts/seed-images.sh`
4. 后端 `mvn clean package -DskipTests` 或 IDEA 里直接 Run
5. 前端 `npm install`

## 4. 常见问题

| 现象 | 原因 / 处理 |
| --- | --- |
| IDEA 启动报 `Communications link failure` | 方案 A：MySQL09 没起（`net start MySQL09`）；方案 B：没双击 `启动数据库.bat` |
| 报 `Unknown database 'digital_medical_application_system'` | 还没导入：双击 `导入数据库到本机MySQL.bat` |
| 登录一直转圈 | 后端没起；或 `.env.development` 地址被改过 |
| 药品图片显示占位图标 | 运行目录 `uploads` 里没图：`bash scripts/seed-images.sh` |
| IDEA 控制台中文乱码 | `Help → Edit Custom VM Options` 加 `-Dfile.encoding=UTF-8` 后重启 IDEA |
| 前端报 `ERR_OSSL_EVP_UNSUPPORTED` | Node 版本不对，确认是 `.dev\node22` 的 22.13 |
| 8080 / 9092 被占 | `bash scripts/dev.sh stop`，或 `netstat -ano \| findstr :8080` 找 pid 结束 |
| IDEA 编译找不到符号 | 确认 Project SDK 是 17；`File → Invalidate Caches / Restart` |

## 5. 命令速查（不想开 IDE 时）

```bash
cd "D:\HermesWorkSpace\Digital Medical Application System"

bash scripts/dev.sh            # 数据库 + 后端 + 前端 一起起
bash scripts/dev.sh status     # 看三个端口状态
bash scripts/dev.sh stop       # 停后端与前端

bash scripts/deploy.sh         # 部署到 Nginx 80 端口（答辩演示用）
bash scripts/deploy.sh stop
```
