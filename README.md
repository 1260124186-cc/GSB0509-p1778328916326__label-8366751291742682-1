# 文体活动管理平台 (Activities Management Platform)

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + Vite + Element Plus | Vue 3.5 / Vite 7 / Element Plus 2.13 |
| 后端 | Spring Boot + Spring Data JPA | Spring Boot 2.7.18 / Java 17 |
| 数据库 | MySQL | 8.0 |
| 容器化 | Docker + Docker Compose | - |
| Web 服务器 | Nginx | - |

---

## 一、项目架构图

### 1.1 前后端与数据库整体架构

```
┌──────────────────────────────────────────────────────────────────────┐
│                        Docker Network (bridge)                       │
│                                                                      │
│  ┌─────────────────────────┐    ┌─────────────────────────────────┐  │
│  │    Frontend Container   │    │       Backend Container         │  │
│  │                         │    │                                 │  │
│  │   ┌─────────────────┐   │    │  ┌──────────────────────────┐   │  │
│  │   │     Nginx :80    │   │    │  │   Spring Boot :8080      │   │  │
│  │   │                 │   │    │  │                          │   │  │
│  │   │  / → Vue3 SPA   │   │    │  │  ┌────────────────────┐ │   │  │
│  │   │  (静态资源托管)   │   │    │  │  │   Controller 层    │ │   │  │
│  │   │                 │   │    │  │  │  AdminController   │ │   │  │
│  │   │  /api → proxy   │───┼────┼──│  │  PublicController  │ │   │  │
│  │   │  pass to        │   │    │  │  └────────┬───────────┘ │   │  │
│  │   │  backend:8080   │   │    │  │           │             │   │  │
│  │   └─────────────────┘   │    │  │  ┌────────▼───────────┐ │   │  │
│  │                         │    │  │  │    Service 层       │ │   │  │
│  │  Host Port: 3000 ──► :80│    │  │  │  ActivityService   │ │   │  │
│  │                         │    │  │  │  CheckinService     │ │   │  │
│  └─────────────────────────┘    │  │  └────────┬───────────┘ │   │  │
│                                  │  │           │             │   │  │
│                                  │  │  ┌────────▼───────────┐ │   │  │
│                                  │  │  │   Repository 层    │ │   │  │
│                                  │  │  │  ActivityRepo      │ │   │  │
│                                  │  │  │  CheckinRecordRepo │ │   │  │
│                                  │  │  └────────┬───────────┘ │   │  │
│                                  │  └───────────┼─────────────┘  │  │
│                                  │              │                │  │
│                                  │  Host Port: 8080 ──► :8080   │  │
│                                  └──────────────┼────────────────┘  │
│                                                 │                   │
│                                                 │ JDBC              │
│                                                 ▼                   │
│                                  ┌──────────────────────────────┐   │
│                                  │      MySQL Container         │   │
│                                  │                              │   │
│                                  │   MySQL 8.0 :3306           │   │
│                                  │   Database: activities_db    │   │
│                                  │                              │   │
│                                  │   ┌────────────────────┐    │   │
│                                  │   │     activity       │    │   │
│                                  │   │   checkin_record   │    │   │
│                                  │   └────────────────────┘    │   │
│                                  │                              │   │
│                                  │   Volume: mysql-data        │   │
│                                  │   Host Port: 3306 ◄── :3306│   │
│                                  └──────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

### 1.2 请求流转链路

```
用户浏览器
    │
    ▼
Nginx (:80 / 端口映射 3000)
    │
    ├── / (静态资源) ──► Vue 3 SPA (index.html + JS/CSS)
    │
    └── /api (API 请求) ──► proxy_pass ──► Spring Boot (:8080)
                                                │
                                                ├── /api/admin/activities/*   (管理端)
                                                └── /api/public/activities/*  (公众端)
                                                        │
                                                        ▼
                                                   MySQL (:3306)
```

---

## 二、数据库设计

### 2.1 ER 图

```
┌──────────────────────────┐          ┌──────────────────────────┐
│        activity          │          │     checkin_record       │
├──────────────────────────┤          ├──────────────────────────┤
│ id           BIGINT (PK) │◄────────┤ id           BIGINT (PK) │
│ title        VARCHAR     │    1:N   │ activity_id  BIGINT (FK) │
│ start_time   DATETIME    │──────────┤ name         VARCHAR(100)│
│ end_time     DATETIME    │          │ checkin_time DATETIME    │
│ description  TEXT        │          └──────────────────────────┘
│ status       VARCHAR(20) │
│ checkin_token VARCHAR(64)│
│ created_at   DATETIME    │
└──────────────────────────┘
```

### 2.2 表结构详解

**activity 表** — 存储活动信息

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, 自增 | 活动唯一标识 |
| title | VARCHAR | NOT NULL | 活动标题 |
| start_time | DATETIME | NOT NULL | 活动开始时间 |
| end_time | DATETIME | NOT NULL | 活动结束时间 |
| description | TEXT | 可选 | 活动说明 |
| status | VARCHAR(20) | NOT NULL, 默认 DRAFT | 活动状态: DRAFT / PUBLISHED / CLOSED |
| checkin_token | VARCHAR(64) | UNIQUE | 发布时生成的签到令牌 |
| created_at | DATETIME | NOT NULL, 不可更新 | 创建时间（JPA 自动填充） |

**checkin_record 表** — 存储签到记录

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, 自增 | 签到记录唯一标识 |
| activity_id | BIGINT | NOT NULL | 关联的活动 ID |
| name | VARCHAR(100) | NOT NULL | 签到人姓名 |
| checkin_time | DATETIME | NOT NULL, 不可更新 | 签到时间（JPA 自动填充） |

### 2.3 活动状态机

```
  DRAFT (草稿)
     │
     │ publishActivity()
     │ 生成 UUID checkinToken
     ▼
  PUBLISHED (已发布)
     │
     │ closeActivity()
     ▼
  CLOSED (已关闭)
```

---

## 三、核心模块实现逻辑

### 3.1 后端模块架构（分层架构）

```
com.activities
├── config/          → 跨域配置 (WebConfig)
├── controller/      → REST API 控制器
│   ├── AdminController      → 管理端 API (/api/admin/activities)
│   └── PublicController     → 公众端 API (/api/public/activities)
├── dto/             → 数据传输对象
│   ├── ActivityDto          → CreateRequest / PublishResponse
│   └── CheckinDto           → Request
├── entity/          → JPA 实体
│   ├── Activity             → 活动实体
│   └── CheckinRecord        → 签到记录实体
├── enums/           → 枚举
│   └── ActivityStatus       → DRAFT / PUBLISHED / CLOSED
├── exception/       → 全局异常处理
│   ├── GlobalExceptionHandler → @RestControllerAdvice 统一异常捕获
│   └── NotFoundException      → 资源未找到异常
├── repository/      → 数据访问层 (Spring Data JPA)
│   ├── ActivityRepository    → findByCheckinToken()
│   └── CheckinRecordRepository → findByActivityIdOrderByCheckinTimeDesc()
└── service/         → 业务逻辑层
    ├── ActivityService       → 活动 CRUD + 发布 + 关闭
    └── CheckinService        → 签到逻辑 + 签到名单查询
```

### 3.2 活动管理模块（ActivityService）

**核心业务逻辑：**

1. **创建活动** (`createActivity`)
   - 校验结束时间不能早于开始时间
   - 创建 Activity 实体，状态初始为 `DRAFT`
   - 通过 JPA 保存到数据库

2. **发布活动** (`publishActivity`) — `@Transactional`
   - 查询活动，校验当前状态（不能重复发布）
   - 生成 `UUID` 作为 `checkinToken`，保证唯一性
   - 状态变更为 `PUBLISHED`
   - 返回 `PublishResponse`（包含 token，前端据此生成签到链接和二维码）

3. **关闭活动** (`closeActivity`) — `@Transactional`
   - 状态变更为 `CLOSED`，此后不允许签到

4. **Token 查询活动** (`getActivityByToken`)
   - 通过 `checkinToken` 查询活动（用于扫码签到场景）
   - 未找到则抛出 `NotFoundException`

### 3.3 签到模块（CheckinService）

**核心业务逻辑：**

1. **签到** (`checkin`) — `@Transactional`
   - 通过 token 查询活动
   - 校验活动状态：`CLOSED` 拒绝，非 `PUBLISHED` 拒绝
   - 校验姓名非空
   - 创建签到记录，关联 `activityId`

2. **签到名单查询** (`getCheckins`)
   - 按签到时间倒序返回指定活动的签到列表

### 3.4 API 接口设计

**管理端 API** (`/api/admin/activities`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | / | 创建活动 |
| GET | / | 获取所有活动列表 |
| GET | /{id} | 获取活动详情 |
| POST | /{id}/publish | 发布活动（生成签到 Token） |
| POST | /{id}/close | 关闭活动 |
| GET | /{id}/checkins | 获取活动签到名单 |

**公众端 API** (`/api/public/activities`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /{token} | 通过签到 Token 获取活动信息 |
| POST | /{token}/checkin | 提交签到 |

### 3.5 全局异常处理

`GlobalExceptionHandler` 通过 `@RestControllerAdvice` 统一拦截：

| 异常类型 | HTTP 状态码 | 场景 |
|----------|-------------|------|
| NotFoundException | 404 | 活动/Token 不存在 |
| IllegalStateException | 400 | 活动状态不合法（如重复发布、已关闭签到） |
| IllegalArgumentException | 400 | 参数校验失败（如时间不合法） |
| Exception | 500 | 未知服务端错误 |

### 3.6 跨域配置

`WebConfig` 允许前端开发环境 `http://localhost:3000` 跨域访问后端 API，支持 GET/POST/PUT/DELETE/OPTIONS 方法。

---

## 四、前端模块架构

### 4.1 目录结构

```
frontend/src
├── api/
│   ├── request.js           → Axios 实例封装 (baseURL: /api, 拦截器)
│   └── activity.js          → 活动/签到 API 方法集合
├── components/
│   └── CreateActivityForm.vue → 创建活动对话框组件
├── router/
│   └── index.js             → Vue Router 路由配置
├── views/
│   ├── admin/
│   │   ├── ActivityList.vue   → 管理端：活动列表页
│   │   └── ActivityDetail.vue → 管理端：活动详情页（含二维码）
│   └── public/
│       ├── Checkin.vue        → 公众端：签到页
│       └── CheckinSuccess.vue → 公众端：签到成功页
├── App.vue                  → 根组件
├── main.js                  → 入口（注册 Element Plus + Router）
└── style.css                → 全局样式
```

### 4.2 路由设计

| 路径 | 组件 | 说明 |
|------|------|------|
| / | → 重定向到 /admin | 默认入口 |
| /admin | ActivityList | 活动管理列表 |
| /admin/activity/:id | ActivityDetail | 活动详情 + 二维码 + 签到名单 |
| /checkin/:token | Checkin | 扫码签到页（移动端优先） |
| /checkin/success | CheckinSuccess | 签到成功页 |

所有路由组件采用 **懒加载** (`() => import(...)`) 方式引入，减小首屏加载体积。

### 4.3 前端核心交互流程

**管理员操作流程：**

```
活动列表页
  ├── 点击「创建活动」→ 弹出 CreateActivityForm 对话框
  │     └── 填写标题 + 时间范围 + 说明 → 调用 createActivity API
  ├── 点击「发布」→ 调用 publishActivity API → 状态变为 PUBLISHED
  └── 点击「详情」→ 进入 ActivityDetail 页
        ├── 显示活动基本信息 + 签到名单
        ├── 显示签到二维码（QRCode.js 生成）
        ├── 显示签到链接（支持一键复制）
        └── 点击「关闭」→ 调用 closeActivity API → 状态变为 CLOSED
```

**参与者操作流程：**

```
扫描二维码 / 访问签到链接 → Checkin 页
  ├── 展示活动信息（标题、时间、说明）
  ├── 输入姓名 → 点击「签到打卡」
  │     └── 调用 checkin API → 成功后跳转 CheckinSuccess 页
  └── 活动未发布或已关闭 → 显示「无法签到」提示
```

### 4.4 HTTP 请求封装

- **request.js**：基于 Axios 封装，`baseURL` 设为 `/api`，超时 5 秒
- **响应拦截器**：自动解包 `response.data`，错误时弹出 Element Plus `ElMessage` 提示
- **Nginx 反向代理**：生产环境下，Nginx 将 `/api` 请求代理至 `http://backend:8080`；开发环境下，Vite 的 `server.proxy` 实现相同功能

---

## 五、响应式设计

### 5.1 响应式设计策略概览

本项目的响应式设计采用 **"双场景自适应"** 策略，核心思路是：管理端面向 PC 桌面使用，公众端（签到页）面向手机移动端使用。两端分别针对其主用场景优化布局，同时保持跨设备可用性。

### 5.2 响应式实现层次

```
┌──────────────────────────────────────────────────────────────┐
│                     响应式设计实现                             │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  1. Viewport 元标签                                          │
│     index.html: <meta name="viewport" content="width=device-│
│     width, initial-scale=1.0">                               │
│     → 确保移动端正确缩放，禁止默认缩放行为                      │
│                                                              │
│  2. Element Plus 组件级响应式                                 │
│     ├── el-table: 固定列(fixed) + 横向滚动自动适配            │
│     ├── el-dialog: width="500px" 弹窗自适应                   │
│     ├── el-col/el-row: 栅格布局 (:span="16" / :span="8")    │
│     ├── el-date-picker: datetimerange 移动端友好选择器        │
│     └── el-result/el-card: 内容自适应容器                     │
│                                                              │
│  3. CSS 弹性布局 (Flexbox)                                   │
│     ├── .header: display:flex + justify-content:space-between│
│     ├── .page-header: display:flex + align-items:center      │
│     └── .info p: display:flex + align-items:center           │
│                                                              │
│  4. CSS 限制最大宽度 + 居中                                   │
│     ├── .activity-list-container: max-width:1200px; margin:auto│
│     ├── .activity-detail-container: max-width:1200px         │
│     ├── .checkin-card: max-width:400px (移动端最优宽度)       │
│     └── .success-card: max-width:400px                       │
│                                                              │
│  5. 垂直居中全屏布局 (签到/成功页)                            │
│     ├── .checkin-container: min-height:100vh                 │
│     │   display:flex; justify-content:center;                │
│     │   align-items:center; padding:20px                     │
│     └── .success-container: 同上                              │
│                                                              │
│  6. @media 查询 (全局样式)                                   │
│     style.css: @media (prefers-color-scheme: light)          │
│     → 适配亮色/暗色系统主题                                   │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### 5.3 管理端响应式设计

**ActivityList.vue（活动列表页）：**
- `max-width: 1200px; margin: 0 auto` — 大屏居中显示，小屏自适应宽度
- `el-table` 使用 `fixed="right"` 固定操作列，小屏幕时表格可横向滚动
- 使用 `min-width` 而非固定 `width`，列宽自适应内容

**ActivityDetail.vue（活动详情页）：**
- 使用 `el-row` + `el-col` 栅格布局：左侧信息区 `:span="16"`，右侧二维码区 `:span="8"`
- Element Plus 的栅格系统在窄屏下会自动堆叠为纵向排列
- `el-table` 签到名单设置固定 `height="400"`，防止名单过长撑开页面

**CreateActivityForm.vue（创建活动对话框）：**
- `el-dialog` 设置 `width="500px"`，在窄屏下自动收缩适配
- 表单元素使用 Element Plus 自适应宽度（`el-input`、`el-date-picker` 默认 100% 宽度）

### 5.4 公众端响应式设计（移动端优先）

**Checkin.vue（签到页）：**
- `min-height: 100vh` + Flexbox 垂直水平居中 — 全屏沉浸式体验
- `max-width: 400px` — 限制卡片最大宽度，在手机上完美展示，在桌面上不会过宽
- `padding: 20px` — 安全边距，防止内容贴边
- `el-button size="large"` + `width: 100%` — 大尺寸按钮，方便手指触控
- `el-input size="large"` — 大尺寸输入框，便于移动端输入
- 渐变背景 `linear-gradient(135deg, #667eea, #764ba2)` — 视觉区分管理端

**CheckinSuccess.vue（签到成功页）：**
- 同样采用 Flexbox 全屏居中布局
- `max-width: 400px` 卡片宽度限制
- 绿色背景 `#f0f9eb` 传达成功语义

### 5.5 响应式设计总结

| 设计手段 | 管理端 | 公众端 |
|----------|--------|--------|
| Viewport Meta | ✅ | ✅ |
| max-width + margin:auto | 1200px | 400px |
| Flexbox 布局 | header 对齐 | 全屏居中 |
| Element Plus 栅格 | el-row/el-col | — |
| Element Plus 组件自适应 | el-table 滚动 | el-card 弹性 |
| 移动端大尺寸控件 | — | size="large" + 100% |
| 系统主题适配 | prefers-color-scheme | — |
| 全屏沉浸式布局 | — | min-height:100vh |

---

## 六、容器化部署架构

### 6.1 Docker Compose 服务编排

```
docker-compose.yml 定义 3 个服务:

┌─────────────────────────────────────────────┐
│               activities-network (bridge)    │
│                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │    db     │  │ backend  │  │ frontend │  │
│  │ MySQL:8.0│  │ Spring:  │  │ Nginx:   │  │
│  │ :3306    │  │ :8080    │  │ :80      │  │
│  └──────────┘  └──────────┘  └──────────┘  │
│       ▲             ▲             ▲          │
│       │             │             │          │
│  mysql-data    depends_on db   depends_on   │
│   volume       (healthy)       backend      │
└─────────────────────────────────────────────┘
```

**关键设计：**
- MySQL 使用 `healthcheck` 确保就绪后 Backend 才启动
- Backend `depends_on: db: condition: service_healthy`
- Frontend `depends_on: backend`
- MySQL 数据持久化到 `mysql-data` volume
- 所有服务在同一 `activities-network` 桥接网络中通信

### 6.2 Nginx 配置

- `/` → 静态资源托管（Vue 3 构建产物），`try_files` 支持 SPA History 模式路由
- `/api` → 反向代理至 `http://backend:8080`，透传真实 IP 头信息

---

## 七、快速启动

### 前置要求
- Docker Desktop 已安装并运行

### 启动步骤
```bash
docker compose up --build
```

### 服务地址
- **前端**: http://localhost:3000
- **后端 API**: http://localhost:8080/api
- **数据库**: localhost:3306 (user: root / pass: root)
