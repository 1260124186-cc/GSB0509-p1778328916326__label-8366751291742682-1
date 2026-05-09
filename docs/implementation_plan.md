# 文体活动管理平台 - 实施方案

## 项目概述

开发一个文体活动管理平台，实现活动发布、二维码签到、签到记录管理和导出功能。项目采用前后端分离架构，后端使用 Java + Spring Boot 2，前端使用 Vue 3，数据库使用 MySQL，全部服务通过 Docker 容器化部署。

---

## 🛠 技术栈

### 后端
- **框架**: Spring Boot 2.7.x
- **Java 版本**: Java 17
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA
- **构建工具**: Maven 3.9
- **其他依赖**:
  - Lombok（简化代码）
  - Apache POI（CSV 导出）
  - Spring Boot Starter Validation（数据验证）

### 前端
- **框架**: Vue 3
- **构建工具**: Vite
- **UI 组件库**: Element Plus
- **其他依赖**:
  - Vue Router（路由管理）
  - Axios（HTTP 请求）
  - QRCode.js（二维码生成）
  - Day.js（时间处理）

### 容器化
- **容器编排**: Docker Compose
- **镜像**:
  - MySQL: `mysql:8.0`
  - Java 构建: `maven:3.9-eclipse-temurin-17-alpine`
  - Java 运行: `eclipse-temurin:17-jre-alpine`
  - Node 构建: `node:20-alpine`
  - Nginx: `nginx:alpine`

---

## 用户需要审阅的内容

> [!IMPORTANT]
> **MVP 范围确认**：本方案暂不实现地理定位功能，仅支持手动填写姓名签到。地理定位功能作为二期需求预留接口扩展性。

> [!WARNING]
> **端口占用提醒**：项目将使用以下端口，请确保这些端口未被占用：
> - 前端：`3000`
> - 后端：`8080`
> - MySQL：`3306`

---

## 📊 数据库设计

### 表结构

#### activity（活动表）

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 主键 | AUTO_INCREMENT |
| title | VARCHAR(255) | 活动标题 | NOT NULL |
| start_time | DATETIME | 活动开始时间 | NOT NULL |
| end_time | DATETIME | 活动结束时间 | NOT NULL |
| description | TEXT | 活动说明 | NULL |
| status | VARCHAR(20) | 活动状态（DRAFT/PUBLISHED/CLOSED） | NOT NULL, DEFAULT 'DRAFT' |
| checkin_token | VARCHAR(64) | 签到令牌（UUID） | UNIQUE |
| created_at | DATETIME | 创建时间 | DEFAULT CURRENT_TIMESTAMP |

#### checkin_record（签到记录表）

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 主键 | AUTO_INCREMENT |
| activity_id | BIGINT | 活动ID（外键） | NOT NULL |
| name | VARCHAR(100) | 签到姓名 | NOT NULL |
| checkin_time | DATETIME | 签到时间 | DEFAULT CURRENT_TIMESTAMP |

**索引设计**:
- `activity_id` 上创建索引（加速查询）
- `checkin_token` 唯一索引（保证唯一性）

---

## 🔌 API 接口设计

### 管理端接口

#### 1. 创建活动
```http
POST /api/admin/activities
Content-Type: application/json

Request Body:
{
  "title": "篮球比赛",
  "startTime": "2026-01-25T14:00:00",
  "endTime": "2026-01-25T17:00:00",
  "description": "校园篮球友谊赛"
}

Response (201 Created):
{
  "id": 1,
  "title": "篮球比赛",
  "startTime": "2026-01-25T14:00:00",
  "endTime": "2026-01-25T17:00:00",
  "description": "校园篮球友谊赛",
  "status": "DRAFT",
  "createdAt": "2026-01-23T06:30:00"
}
```

#### 2. 发布活动
```http
POST /api/admin/activities/{id}/publish

Response (200 OK):
{
  "id": 1,
  "status": "PUBLISHED",
  "checkinToken": "8f3e5a7b-2c9d-4e1a-9b6f-3d8c5e7a2b4f",
  "checkinUrl": "http://localhost:3000/checkin/8f3e5a7b-2c9d-4e1a-9b6f-3d8c5e7a2b4f"
}
```

#### 3. 关闭活动
```http
POST /api/admin/activities/{id}/close

Response (200 OK):
{
  "id": 1,
  "status": "CLOSED"
}
```

#### 4. 查询活动列表
```http
GET /api/admin/activities

Response (200 OK):
{
  "activities": [
    {
      "id": 1,
      "title": "篮球比赛",
      "startTime": "2026-01-25T14:00:00",
      "endTime": "2026-01-25T17:00:00",
      "status": "PUBLISHED",
      "checkinCount": 15
    }
  ]
}
```

#### 5. 查看签到名单
```http
GET /api/admin/activities/{id}/checkins

Response (200 OK):
{
  "activityTitle": "篮球比赛",
  "totalCount": 15,
  "checkins": [
    {
      "id": 1,
      "name": "张三",
      "checkinTime": "2026-01-25T14:05:30"
    },
    {
      "id": 2,
      "name": "李四",
      "checkinTime": "2026-01-25T14:06:15"
    }
  ]
}
```



---

### 参与者端接口

#### 1. 获取活动信息
```http
GET /api/public/activities/{token}

Response (200 OK):
{
  "title": "篮球比赛",
  "startTime": "2026-01-25T14:00:00",
  "endTime": "2026-01-25T17:00:00",
  "description": "校园篮球友谊赛",
  "status": "PUBLISHED"
}
```

#### 2. 提交签到
```http
POST /api/public/activities/{token}/checkin
Content-Type: application/json

Request Body:
{
  "name": "张三"
}

Response (201 Created):
{
  "id": 1,
  "name": "张三",
  "checkinTime": "2026-01-25T14:05:30",
  "message": "签到成功"
}
```

**错误响应示例**:
```json
{
  "error": "活动已关闭，无法签到",
  "code": "ACTIVITY_CLOSED"
}
```

---

## 📂 项目变更详情

### 新建项目结构

```
activities/
├── docker-compose.yml          [NEW]
├── .gitignore                  [NEW]
├── .dockerignore               [NEW]
├── README.md                   [NEW]
├── backend/                    [NEW]
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/
│           │       └── activities/
│           │           ├── ActivityApplication.java
│           │           ├── entity/
│           │           │   ├── Activity.java
│           │           │   └── CheckinRecord.java
│           │           ├── repository/
│           │           │   ├── ActivityRepository.java
│           │           │   └── CheckinRecordRepository.java
│           │           ├── service/
│           │           │   ├── ActivityService.java
│           │           │   └── CheckinService.java
│           │           ├── controller/
│           │           │   ├── AdminController.java
│           │           │   └── PublicController.java
│           │           ├── dto/
│           │           │   ├── CreateActivityRequest.java
│           │           │   ├── CheckinRequest.java
│           │           │   └── ActivityResponse.java
│           │           └── exception/
│           │               └── GlobalExceptionHandler.java
│           └── resources/
│               ├── application.yml
│               └── data.sql
└── frontend/                   [NEW]
    ├── Dockerfile
    ├── .dockerignore
    ├── package.json
    ├── vite.config.js
    ├── index.html
    ├── nginx.conf
    └── src/
        ├── main.js
        ├── App.vue
        ├── router/
        │   └── index.js
        ├── api/
        │   └── activity.js
        ├── views/
        │   ├── admin/
        │   │   ├── ActivityList.vue
        │   │   └── ActivityDetail.vue
        │   └── public/
        │       ├── Checkin.vue
        │       └── CheckinSuccess.vue
        └── components/
            ├── CreateActivityForm.vue
            └── LoadingSpinner.vue
```

---

## 🎨 前端页面设计

### 管理端

#### 1. 活动列表页 (`/admin`)
**功能**:
- 展示活动列表（表格形式）
- 显示活动状态标签（草稿/已发布/已关闭）
- "创建活动"按钮
- 每行操作按钮：发布 / 查看详情 / 关闭

**UI 要点**:
- 使用 Element Plus Table 组件
- 状态使用不同颜色的 Tag 区分
- 响应式布局，移动端改为卡片布局

#### 2. 活动详情页 (`/admin/activity/:id`)
**功能**:
- 显示活动基本信息
- 显示签到二维码（可放大）
- 显示签到链接（一键复制）
- 签到人数统计（实时更新）
- 签到名单表格
- 导出 CSV 按钮

**UI 要点**:
- 卡片式布局
- 二维码居中显示，带阴影效果
- 复制链接按钮带 Hover 效果和复制成功提示

### 参与者端

#### 3. 签到页 (`/checkin/:token`)
**功能**:
- 显示活动信息
- 姓名输入框（必填验证）
- 提交按钮

**UI 要点**:
- 简洁大方的表单设计
- 移动端优先（大字体、大按钮）
- Loading 状态禁用按钮

#### 4. 签到成功页 (`/checkin/success`)
**功能**:
- 显示"已签到"提示
- 显示签到时间
- 成功图标动画

**UI 要点**:
- 使用 Element Plus Result 组件
- 绿色成功主题
- 可选：3 秒后自动跳转或关闭页面

---

## 🐳 Docker 容器化方案

### docker-compose.yml

```yaml
version: '3.8'

services:
  db:
    image: mysql:8.0
    container_name: activities-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: activities_db
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - activities-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    container_name: activities-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/activities_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
    networks:
      - activities-network

  frontend:
    build: ./frontend
    container_name: activities-frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
    networks:
      - activities-network

networks:
  activities-network:
    driver: bridge

volumes:
  mysql-data:
```

### 后端 Dockerfile

```dockerfile
# 构建阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 前端 Dockerfile

```dockerfile
# 构建阶段
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm config set registry https://registry.npmmirror.com
RUN npm ci
COPY . .
RUN npm run build

# 生产阶段
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## ✅ 验证计划

### 自动化测试

#### 1. 后端 API 测试
**工具**: Postman / curl

**测试步骤**:
```bash
# 1. 创建活动
curl -X POST http://localhost:8080/api/admin/activities \
  -H "Content-Type: application/json" \
  -d '{"title":"测试活动","startTime":"2026-01-25T14:00:00","endTime":"2026-01-25T17:00:00"}'

# 2. 发布活动（使用上一步返回的 ID）
curl -X POST http://localhost:8080/api/admin/activities/1/publish

# 3. 获取活动信息（使用返回的 token）
curl http://localhost:8080/api/public/activities/{token}

# 4. 提交签到
curl -X POST http://localhost:8080/api/public/activities/{token}/checkin \
  -H "Content-Type: application/json" \
  -d '{"name":"张三"}'

# 5. 查看签到名单
curl http://localhost:8080/api/admin/activities/1/checkins

# 6. 导出 CSV
curl -O http://localhost:8080/api/admin/activities/1/checkins/export
```

#### 2. Docker 容器启动测试
```bash
# 在项目根目录执行
docker compose down -v
docker compose up --build

# 验证所有容器正常运行
docker compose ps

# 验证日志无错误
docker compose logs
```

### 手动验证

#### 1. 前端功能验证
1. 访问 http://localhost:3000/admin
2. 点击"创建活动"按钮，填写表单并提交
3. 在活动列表中找到新创建的活动，点击"发布"
4. 点击"查看详情"，验证二维码和签到链接显示正确
5. 复制签到链接，在新标签页打开
6. 填写姓名并提交签到
7. 返回活动详情页，验证签到记录已显示
8. 点击"导出 CSV"，验证文件下载成功

#### 2. 响应式适配验证
1. 打开浏览器开发者工具
2. 切换到移动设备视图（iPhone / Android）
3. 验证所有页面布局正常，无横向滚动条
4. 验证按钮大小适中，易于点击

#### 3. 二维码扫描验证
1. 使用手机微信扫描管理端显示的二维码
2. 验证能正常打开签到页面
3. 填写姓名并提交
4. 验证签到成功并显示成功页面

#### 4. 数据持久化验证
```bash
# 停止容器
docker compose down

# 重新启动
docker compose up

# 访问前端，验证之前创建的活动和签到记录仍然存在
```

---

## 🎯 技术亮点

- ✅ **100% Docker 容器化**：一键启动，零依赖
- ✅ **现代化 UI**：基于 Element Plus，响应式设计
- ✅ **RESTful API**：清晰的接口设计，前后端分离
- ✅ **数据持久化**：MySQL + Docker Volume
- ✅ **完整的错误处理**：全局异常处理 + Toast 提示
- ✅ **二维码签到**：扫码即签，移动端友好
- ✅ **CSV 导出**：一键导出签到名单
- ✅ **中文语言**：代码、注释、文档全中文

---

## ⚠️ 注意事项

1. **端口占用检查**：启动前确保 3000、8080、3306 端口未被占用
2. **数据库初始化时间**：首次启动时 MySQL 需要 10-30 秒初始化
3. **网络连通性**：前端访问后端必须使用服务名 `backend`，不能使用 `localhost`
4. **字符编码**：确保 MySQL 使用 UTF-8 编码，避免中文乱码
5. **时区设置**：统一使用 `Asia/Shanghai` 时区

---

## 📅 预计开发时间

- 项目初始化：0.5 天
- 后端开发：1.5 天
- 前端开发：1.5 天
- Docker 容器化：0.5 天
- 测试和文档：1 天

**总计**：约 5 个工作日
