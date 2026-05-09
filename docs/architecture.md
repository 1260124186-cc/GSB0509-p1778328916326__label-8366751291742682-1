# 系统架构文档 (System Architecture)

## 1. 系统概览 (System Overview)
本系统是一个基于 Docker 容器化的**文体活动管理平台**，旨在提供活动发布、二维码签到、实时名单管理等功能。系统采用前后端分离架构，前端使用 Vue 3 构建，后端基于 Spring Boot 2 实现，数据存储采用 MySQL 8.0。

## 2. 架构图 (Architecture Diagram)

```mermaid
graph TD
    User[参与者 (Mobile)] -->|HTTP/Web| Nginx[Nginx (Frontend Host)]
    Admin[管理员 (PC)] -->|HTTP/Web| Nginx
    
    subgraph Docker Container Network
        Nginx -->|Proxy /api| Backend[Spring Boot Backend]
        Backend -->|JDBC| DB[(MySQL 8.0)]
    end
```

## 3. 技术栈 (Technology Stack)

### 前端 (Frontend)
- **Framework**: Vue 3 (Composition API)
- **Build Tool**: Vite 4.x
- **UI Library**: Element Plus
- **HTTP Client**: Axios
- **Routing**: Vue Router 4.x
- **Utils**: qrcode.js (二维码生成), dayjs (日期处理)

### 后端 (Backend)
- **Framework**: Spring Boot 2.7.x
- **Language**: Java 17
- **ORM**: Spring Data JPA (Hibernate)
- **Database Driver**: MySQL Connector/J 8.0.33
- **Tools**: Lombok

### 基础设施 (Infrastructure)
- **Containerization**: Docker, Docker Compose
- **Web Server**: Nginx (Alpine based)

## 4. 目录结构 (Directory Structure)

```
activities/
├── backend/                # 后端工程
│   ├── src/main/java/      # Java 源代码
│   ├── src/main/resources/ # 配置文件和静态资源
│   └── Dockerfile          # 后端构建镜像定义
├── frontend/               # 前端工程
│   ├── src/                # Vue 源代码
│   ├── vite.config.js      # Vite 配置
│   └── Dockerfile          # 前端构建镜像定义 (含 Nginx 配置)
├── docs/                   # 项目文档
├── docker-compose.yml      # 容器编排文件
└── README.md               # 项目入口文档
```

## 5. 部署架构 (Deployment)
系统通过 `docker-compose.yml` 编排三个核心服务：
1. **db**: MySQL 数据库服务，挂载本地卷实现数据持久化。
2. **backend**: 后端服务，依赖 `db` 服务，暴露 8080 端口（仅内部或开发用）。
3. **frontend**: 前端 Nginx 服务，依赖 `backend` 服务，暴露 3000 端口供用户访问，并反向代理 `/api` 请求至后端。
