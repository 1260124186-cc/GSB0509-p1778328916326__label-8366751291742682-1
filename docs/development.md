# 开发指南 (Development Guide)

## 1. 环境准备 (Prerequisites)
开始开发前，请确保您的开发环境满足以下要求：

- **Java**: JDK 17 或更高版本
- **Node.js**: LTS 版本 (推荐 v16+)
- **Maven**: 3.8+ (项目包含 mvnw wrapper)
- **Docker**: Docker Desktop (用于容器化部署)
- **IDE**: IntelliJ IDEA (推荐) 或 VS Code

## 2. 本地开发 (Local Development)

### 后端 (Backend)
1. 进入后端目录：
   ```bash
   cd backend
   ```
2. 启动 Spring Boot 应用：
   ```bash
   ./mvnw spring-boot:run
   ```
   后端服务将在 `http://localhost:8080` 启动。

### 前端 (Frontend)
1. 进入前端目录：
   ```bash
   cd frontend
   ```
2. 安装依赖：
   ```bash
   npm install
   ```
3. 启动开发服务器：
   ```bash
   npm run dev
   ```
   前端服务将在 `http://localhost:3000` 启动。

⚠️ **注意**: 本地开发时，前端配置的代理会将 `/api` 请求转发到 `http://localhost:8080`。确保后端服务已启动。

## 3. 容器化部署 (Docker Deployment)

### 一键启动
在项目根目录执行：
```bash
docker compose up --build
```
这将同时启动 MySQL、后端 API 和前端 Nginx 服务。

### 服务访问
- **前端**: `http://localhost:3000`
- **后端 API**: `http://localhost:8080/api`
- **数据库**: 端口映射到 `3306`

## 4. 常用命令 (Common Commands)

### Maven (Backend)
- **清理**: `./mvnw clean`
- **打包**: `./mvnw package`
- **运行测试**: `./mvnw test`

### NPM (Frontend)
- **构建生产版本**: `npm run build`
- **预览生产构建**: `npm run preview`
- **Lint 检查**: `npm run lint`

## 5. 配置文件 (Configuration)

### 后端
- `backend/src/main/resources/application.yml`: 核心配置
  - `server.port`: 服务端口
  - `spring.datasource`: 数据库连接配置

### 前端
- `frontend/vite.config.js`: Vite 配置
  - `server.proxy`: API 代理配置
