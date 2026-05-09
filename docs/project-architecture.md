# 文体活动管理平台 — 项目架构与核心模块详解

## 一、项目概览

本项目是一个**文体活动管理与签到平台**，支持管理员创建/发布/关闭活动，参与者通过扫码或链接进行签到。采用前后端分离架构，通过 Docker Compose 容器化部署。

### 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + Vite + Element Plus | Vue 3.5 / Vite 7 / Element Plus 2.13 |
| 后端 | Spring Boot + Spring Data JPA + Lombok | Spring Boot 2.7.18 / Java 17 |
| 数据库 | MySQL | 8.0 |
| 容器化 | Docker + Docker Compose | - |
| Web 服务器 | Nginx | - |

---

## 二、前后端与数据库整体架构图

### 2.1 系统整体架构

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         Docker Network (bridge)                          │
│                                                                          │
│  ┌──────────────────────────┐   ┌─────────────────────────────────────┐  │
│  │   Frontend Container     │   │        Backend Container            │  │
│  │                          │   │                                     │  │
│  │  ┌────────────────────┐  │   │  ┌──────────────────────────────┐  │  │
│  │  │   Nginx :80        │  │   │  │   Spring Boot :8080          │  │  │
│  │  │                    │  │   │  │                              │  │  │
│  │  │  / → Vue3 SPA      │  │   │  │  ┌────────────────────────┐ │  │  │
│  │  │  (静态资源托管)      │  │   │  │  │   Controller 层        │ │  │  │
│  │  │                    │  │   │  │  │  ┌──────────────────┐  │ │  │  │
│  │  │  /api → proxy      │──┼───┼──│  │  │ AdminController  │  │ │  │  │
│  │  │  pass to           │  │   │  │  │  │ PublicController │  │ │  │  │
│  │  │  backend:8080      │  │   │  │  │  └──────────────────┘  │ │  │  │
│  │  └────────────────────┘  │   │  │  └───────────┬────────────┘ │  │  │
│  │                          │   │  │              │              │  │  │
│  │  Host Port: 3000 ──► :80│   │  │  ┌───────────▼────────────┐ │  │  │
│  │                          │   │  │  │     Service 层          │ │  │  │
│  └──────────────────────────┘   │  │  │  ActivityService       │ │  │  │
│                                  │  │  │  CheckinService        │ │  │  │
│                                  │  │  └───────────┬────────────┘ │  │  │
│                                  │  │              │              │  │  │
│                                  │  │  ┌───────────▼────────────┐ │  │  │
│                                  │  │  │    Repository 层       │ │  │  │
│                                  │  │  │  ActivityRepository    │ │  │  │
│                                  │  │  │  CheckinRecordRepo     │ │  │  │
│                                  │  │  └───────────┬────────────┘ │  │  │
│                                  │  └──────────────┼──────────────┘  │  │
│                                  │                 │                 │  │
│                                  │  Host Port: 8080 ──► :8080       │  │
│                                  └─────────────────┼─────────────────┘  │
│                                                     │                  │
│                                                     │ JDBC             │
│                                                     ▼                  │
│                                  ┌──────────────────────────────────┐  │
│                                  │       MySQL Container            │  │
│                                  │                                  │  │
│                                  │   MySQL 8.0 :3306               │  │
│                                  │   Database: activities_db        │  │
│                                  │                                  │  │
│                                  │   ┌──────────────────────┐      │  │
│                                  │   │     activity         │      │  │
│                                  │   │   checkin_record     │      │  │
│                                  │   └──────────────────────┘      │  │
│                                  │                                  │  │
│                                  │   Volume: mysql-data            │  │
│                                  │   Host Port: 3306 ◄── :3306    │  │
│                                  └──────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────┘
```

### 2.2 请求流转链路

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

### 2.3 前后端通信机制

**开发环境**：Vite Dev Server 的 `server.proxy` 将 `/api` 请求代理到 `http://backend:8080`（`vite.config.js:9-14`）。

**生产环境**：Nginx 反向代理将 `/api` 请求转发至 `http://backend:8080`（`nginx.conf:11-16`），同时透传 `X-Real-IP`、`X-Forwarded-For` 头信息。

前端 Axios 实例统一设置 `baseURL: '/api'`，所有 API 请求自动携带此前缀，后端 Controller 的 `@RequestMapping` 也以 `/api` 开头，形成一致的路径映射。

---

## 三、数据库架构

### 3.1 ER 图

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

### 3.2 表结构详解

**activity 表** — 活动信息（`Activity.java:13-41`）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, 自增 (IDENTITY) | 活动唯一标识 |
| title | VARCHAR | NOT NULL | 活动标题 |
| start_time | DATETIME | NOT NULL | 活动开始时间 |
| end_time | DATETIME | NOT NULL | 活动结束时间 |
| description | TEXT | 可选 | 活动说明 |
| status | VARCHAR(20) | NOT NULL, 默认 DRAFT | 活动状态枚举 (STRING 存储) |
| checkin_token | VARCHAR(64) | UNIQUE | 发布时生成的签到令牌 |
| created_at | DATETIME | NOT NULL, 不可更新 | 创建时间 (@CreatedDate 自动填充) |

**checkin_record 表** — 签到记录（`CheckinRecord.java:13-28`）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, 自增 (IDENTITY) | 签到记录唯一标识 |
| activity_id | BIGINT | NOT NULL | 关联的活动 ID |
| name | VARCHAR(100) | NOT NULL | 签到人姓名 |
| checkin_time | DATETIME | NOT NULL, 不可更新 | 签到时间 (@CreatedDate 自动填充) |

### 3.3 活动状态机

```
  DRAFT (草稿)
     │
     │ publishActivity()  →  生成 UUID checkinToken
     ▼
  PUBLISHED (已发布)  ← 可扫码签到
     │
     │ closeActivity()
     ▼
  CLOSED (已关闭)  ← 拒绝签到
```

状态定义在 `ActivityStatus.java:3-6`，使用 `@Enumerated(EnumType.STRING)` 存入数据库，保证可读性。

---

## 四、核心模块实现逻辑

### 4.1 后端分层架构总览

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

### 4.2 活动管理模块 (ActivityService)

**源文件**: `service/ActivityService.java:18-79`

#### 创建活动 (`createActivity`, 第22-36行)

```
请求流入 → Controller 接收 CreateRequest DTO
    │
    ▼
校验: endTime 不能早于 startTime (否则抛 IllegalArgumentException)
    │
    ▼
构建 Activity 实体:
  - title / startTime / endTime / description 从请求填充
  - status 初始为 DRAFT
  - createdAt 由 LocalDateTime.now() 设置
    │
    ▼
activityRepository.save() → 持久化到 MySQL
    │
    ▼
返回 Activity 实体 (含自增 id)
```

#### 发布活动 (`publishActivity`, 第48-66行)

```
请求流入 → @Transactional 开启事务
    │
    ▼
查询活动 → getActivity(id) → 未找到则抛 NotFoundException
    │
    ▼
校验: 当前状态不能为 PUBLISHED (否则抛 IllegalStateException)
    │
    ▼
生成 UUID token → 设置到 activity.checkinToken
    │
    ▼
状态变更为 PUBLISHED → activityRepository.save()
    │
    ▼
返回 PublishResponse { id, checkinToken, checkinUrl }
  → 前端据此生成签到链接和二维码
```

#### 关闭活动 (`closeActivity`, 第69-73行)

```
请求流入 → @Transactional 开启事务
    │
    ▼
查询活动 → getActivity(id)
    │
    ▼
状态变更为 CLOSED → activityRepository.save()
    │
    ▼
返回更新后的 Activity
```

#### Token 查询活动 (`getActivityByToken`, 第75-78行)

```
activityRepository.findByCheckinToken(token)
    │
    ├── 找到 → 返回 Activity
    └── 未找到 → 抛出 NotFoundException("Invalid checkin token")
```

### 4.3 签到模块 (CheckinService)

**源文件**: `service/CheckinService.java:17-48`

#### 签到 (`checkin`, 第23-43行)

```
请求流入 → @Transactional 开启事务
    │
    ▼
通过 token 查询活动 → activityService.getActivityByToken(token)
    │
    ▼
状态校验:
  ├── CLOSED → 抛 IllegalStateException("Activity is closed")
  ├── 非 PUBLISHED → 抛 IllegalStateException("Activity is not published")
  └── PUBLISHED → 继续
    │
    ▼
姓名校验: name 不能为空 (否则抛 IllegalArgumentException)
    │
    ▼
构建 CheckinRecord:
  - activityId 关联活动
  - name 从请求填充 (trim 处理)
  - checkinTime 由 LocalDateTime.now() 设置
    │
    ▼
checkinRecordRepository.save() → 持久化签到记录
    │
    ▼
返回 CheckinRecord (含自增 id)
```

#### 签到名单查询 (`getCheckins`, 第45-47行)

```
checkinRecordRepository.findByActivityIdOrderByCheckinTimeDesc(activityId)
    │
    ▼
返回按签到时间倒序排列的签到记录列表
```

### 4.4 Controller 层 API 设计

**AdminController** (`controller/AdminController.java:22-65`)

| 方法 | 路径 | 说明 | 返回 |
|------|------|------|------|
| POST | /api/admin/activities | 创建活动 | 201 + Activity |
| GET | /api/admin/activities | 获取所有活动 | 200 + List\<Activity\> |
| GET | /api/admin/activities/{id} | 获取活动详情 | 200 + Activity |
| POST | /api/admin/activities/{id}/publish | 发布活动 | 200 + PublishResponse |
| POST | /api/admin/activities/{id}/close | 关闭活动 | 200 + Activity |
| GET | /api/admin/activities/{id}/checkins | 获取签到名单 | 200 + {activityTitle, totalCount, checkins} |

**PublicController** (`controller/PublicController.java:16-29`)

| 方法 | 路径 | 说明 | 返回 |
|------|------|------|------|
| GET | /api/public/activities/{token} | 通过 Token 获取活动 | 200 + Activity |
| POST | /api/public/activities/{token}/checkin | 提交签到 | 201 + CheckinRecord |

### 4.5 全局异常处理

**GlobalExceptionHandler** (`exception/GlobalExceptionHandler.java:11-43`) 通过 `@RestControllerAdvice` 统一拦截异常，返回 `{ "error": "message" }` 格式的 JSON 响应：

| 异常类型 | HTTP 状态码 | 触发场景 |
|----------|-------------|----------|
| NotFoundException | 404 | 活动/Token 不存在 |
| IllegalStateException | 400 | 活动状态不合法 (重复发布、已关闭签到) |
| IllegalArgumentException | 400 | 参数校验失败 (时间不合法、姓名为空) |
| Exception | 500 | 未知服务端错误 |

### 4.6 Repository 层

基于 Spring Data JPA 的 `JpaRepository` 接口，自动生成实现：

- **ActivityRepository** (`repository/ActivityRepository.java:7-9`): 继承 `JpaRepository<Activity, Long>`，自定义方法 `findByCheckinToken(String)` — Spring Data 根据 方法名自动生成 SQL 查询。
- **CheckinRecordRepository** (`repository/CheckinRecordRepository.java:7-11`): 继承 `JpaRepository<CheckinRecord, Long>`，自定义方法 `findByActivityIdOrderByCheckinTimeDesc` 和 `findByActivityIdOrderByCheckinTimeAsc`。

---

## 五、前端模块架构

### 5.1 目录结构

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
│   │   └── ActivityDetail.vue → 管理端：活动详情页 (含二维码)
│   └── public/
│       ├── Checkin.vue        → 公众端：签到页
│       └── CheckinSuccess.vue → 公众端：签到成功页
├── App.vue                  → 根组件 (仅 <router-view>)
├── main.js                  → 入口 (注册 Element Plus + Router + Icons)
└── style.css                → 全局样式
```

### 5.2 路由设计

**源文件**: `router/index.js:3-24`

| 路径 | 组件 | 加载方式 | 说明 |
|------|------|----------|------|
| / | — | redirect | 重定向到 /admin |
| /admin | ActivityList | 懒加载 | 活动管理列表 |
| /admin/activity/:id | ActivityDetail | 懒加载 | 活动详情 + 二维码 + 签到名单 |
| /checkin/:token | Checkin | 懒加载 | 扫码签到页 (移动端优先) |
| /checkin/success | CheckinSuccess | 懒加载 | 签到成功页 |

所有路由组件采用 **`() => import(...)` 懒加载**，减小首屏加载体积，按需加载各页面模块。

### 5.3 HTTP 请求封装

**request.js** (`api/request.js:1-16`):

```
Axios 实例配置:
  - baseURL: '/api'          → 所有请求自动加 /api 前缀
  - timeout: 5000            → 5秒超时
  - 响应拦截器:               → 自动解包 response.data
  - 错误拦截器:               → ElMessage.error 弹出错误提示
```

**activity.js** (`api/activity.js:1-14`): 封装了 8 个 API 方法，与后端 Controller 一一对应：

```
管理端:
  createActivity(data)    → POST   /admin/activities
  getActivities()         → GET    /admin/activities
  getActivity(id)         → GET    /admin/activities/{id}
  publishActivity(id)     → POST   /admin/activities/{id}/publish
  closeActivity(id)       → POST   /admin/activities/{id}/close
  getCheckins(id)         → GET    /admin/activities/{id}/checkins

公众端:
  getActivityByToken(token) → GET   /public/activities/{token}
  checkin(token, data)      → POST  /public/activities/{token}/checkin
```

### 5.4 核心页面交互流程

**管理员流程**:

```
ActivityList (活动列表页)
  │
  ├── 点击「创建活动」→ CreateActivityForm 对话框
  │     └── 填写标题 + 时间范围 + 说明 → createActivity API → 201
  │
  ├── 点击「发布」→ ElMessageBox 确认 → publishActivity API → 状态 → PUBLISHED
  │
  ├── 点击「关闭」→ ElMessageBox 确认 → closeActivity API → 状态 → CLOSED
  │
  └── 点击「详情」→ router.push(/admin/activity/{id})
                      │
                      ▼
                ActivityDetail (活动详情页)
                  ├── 左侧 el-col :span="16"
                  │     ├── 基本信息 (el-descriptions)
                  │     └── 签到名单 (el-table, height=400 固定滚动)
                  │
                  └── 右侧 el-col :span="8"
                        ├── PUBLISHED 状态:
                        │     ├── 二维码 (qrcode.js toDataURL 生成)
                        │     ├── 签到链接 (可一键复制到剪贴板)
                        │     └── 局域网访问提示
                        └── DRAFT 状态:
                              └── "未发布" 提示 + 「立即发布」按钮
```

**参与者流程**:

```
扫描二维码 / 访问签到链接
    │
    ▼
Checkin 页 (/checkin/:token)
    │
    ├── onMounted → getActivityByToken(token) → 获取活动信息
    │
    ├── PUBLISHED 状态:
    │     ├── 展示活动标题 + 时间 + 说明
    │     ├── 输入姓名 (el-input size="large")
    │     └── 点击「签到打卡」→ checkin API
    │           └── 成功 → router.push(/checkin/success?name=xx&time=xx)
    │
    └── 非 PUBLISHED 状态:
          └── el-result icon="warning" → "无法签到"
```

### 5.5 组件设计亮点

**CreateActivityForm** (`components/CreateActivityForm.vue`):

- 使用 `v-model` 双向绑定实现对话框显示/隐藏的父子通信
- `computed` 的 getter/setter 模式实现 `modelValue` 的双向绑定
- `el-date-picker type="datetimerange"` 一站式选择时间范围
- 表单校验通过 `el-form :rules` 实现，`formRef.validate()` 提交前校验

---

## 六、响应式设计详解

### 6.1 设计策略：双场景自适应

本项目的响应式设计采用 **"双场景自适应"** 策略：

- **管理端** (`/admin/*`) → 面向 **PC 桌面**使用，优化大屏展示
- **公众端** (`/checkin/*`) → 面向 **手机移动端**使用，优化触控体验

两端分别针对其主用场景优化布局，同时保持跨设备可用性。

### 6.2 响应式实现层次

```
┌──────────────────────────────────────────────────────────────────────┐
│                       响应式设计实现 (5个层次)                         │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  第1层: Viewport 元标签 (index.html)                                  │
│    <meta name="viewport" content="width=device-width, initial-scale=1.0">
│    → 确保移动端正确缩放，禁止默认缩放行为                              │
│                                                                      │
│  第2层: Element Plus 组件级响应式                                     │
│    ├── el-table: fixed="right" 固定操作列 + 横向滚动自动适配          │
│    ├── el-dialog: width="500px" 弹窗自适应窄屏                        │
│    ├── el-col/el-row: 栅格布局 (:span="16" / :span="8")              │
│    ├── el-date-picker: datetimerange 移动端友好选择器                 │
│    └── el-result/el-card: 内容自适应容器                              │
│                                                                      │
│  第3层: CSS Flexbox 弹性布局                                         │
│    ├── .header: display:flex + justify-content:space-between         │
│    ├── .page-header: display:flex + align-items:center + gap:20px    │
│    └── .info p: display:flex + align-items:center + gap:8px          │
│                                                                      │
│  第4层: max-width 限制 + 居中                                        │
│    ├── .activity-list-container: max-width:1200px; margin:0 auto     │
│    ├── .activity-detail-container: max-width:1200px                  │
│    ├── .checkin-card: max-width:400px (移动端最优宽度)                │
│    └── .success-card: max-width:400px                                │
│                                                                      │
│  第5层: @media 查询 + 全屏沉浸式布局                                  │
│    ├── style.css: @media (prefers-color-scheme: light)               │
│    ├── .checkin-container: min-height:100vh + flexbox 居中           │
│    └── .success-container: min-height:100vh + flexbox 居中           │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

### 6.3 管理端响应式实现

#### ActivityList.vue (活动列表页)

```css
/* style.css 第133-143行 */
.activity-list-container {
  padding: 20px;
  max-width: 1200px;    /* 大屏居中显示，限制最大宽度 */
  margin: 0 auto;        /* 水平居中 */
}
.header {
  display: flex;                 /* 弹性布局 */
  justify-content: space-between; /* 标题和按钮两端对齐 */
  align-items: center;           /* 垂直居中 */
  margin-bottom: 20px;
}
```

- `el-table` 使用 `fixed="right"` 固定操作列，小屏幕时表格可横向滚动
- 列宽使用 `min-width` 而非固定 `width`，列宽自适应内容

#### ActivityDetail.vue (活动详情页)

```css
/* style.css 第180-184行 */
.activity-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
```

- `el-row` + `el-col` 栅格布局：左侧信息区 `:span="16"`，右侧二维码区 `:span="8"`
- Element Plus 的栅格系统在窄屏下会自动堆叠为纵向排列
- 签到名单 `el-table` 设置固定 `height="400"`，防止名单过长撑开页面

#### CreateActivityForm.vue (创建活动对话框)

- `el-dialog` 设置 `width="500px"`，在窄屏下自动收缩适配
- 表单元素使用 Element Plus 自适应宽度（`el-input`、`el-date-picker` 默认 100% 宽度）

### 6.4 公众端响应式实现（移动端优先）

#### Checkin.vue (签到页)

```css
/* style.css 第97-109行 */
.checkin-container {
  min-height: 100vh;              /* 全屏高度 */
  display: flex;
  justify-content: center;        /* 水平居中 */
  align-items: center;            /* 垂直居中 */
  padding: 20px;                  /* 安全边距，防止贴边 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);  /* 渐变背景 */
}
.checkin-card {
  width: 100%;
  max-width: 400px;               /* 限制卡片最大宽度 */
  border-radius: 12px;            /* 圆角 */
}
```

关键移动端优化点：
- **全屏沉浸式布局**：`min-height: 100vh` + Flexbox 垂直水平居中
- **卡片宽度限制**：`max-width: 400px` — 手机上完美展示，桌面上不会过宽
- **安全边距**：`padding: 20px` 防止内容贴边
- **大尺寸控件**：`el-button size="large"` + `width: 100%` — 方便手指触控
- **大尺寸输入框**：`el-input size="large"` — 便于移动端输入
- **渐变背景**：`linear-gradient(135deg, #667eea, #764ba2)` — 视觉区分管理端

#### CheckinSuccess.vue (签到成功页)

```css
/* style.css 第42-54行 */
.success-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: #f0f9eb;            /* 绿色背景传达成功语义 */
}
.success-card {
  width: 100%;
  max-width: 400px;
  border-radius: 12px;
}
```

### 6.5 全局主题适配

**style.css** (`style.css:68-79`):

```css
@media (prefers-color-scheme: light) {
  :root {
    color: #213547;
    background-color: #ffffff;
  }
  a:hover {
    color: #747bff;
  }
  button {
    background-color: #f9f9f9;
  }
}
```

通过 `@media (prefers-color-scheme: light)` 媒体查询，自动适配操作系统的亮色/暗色主题。

### 6.6 响应式设计总结

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
| 渐变/语义化背景 | — | 紫色渐变 / 绿色成功 |

---

## 七、容器化部署架构

### 7.1 Docker Compose 服务编排

**源文件**: `docker-compose.yml`

```
┌─────────────────────────────────────────────────────┐
│            activities-network (bridge)               │
│                                                      │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐        │
│  │    db     │   │ backend  │   │ frontend │        │
│  │ MySQL:8.0│   │ Spring:  │   │ Nginx:   │        │
│  │ :3306    │   │ :8080    │   │ :80      │        │
│  └──────────┘   └──────────┘   └──────────┘        │
│       ▲              ▲              ▲                │
│       │              │              │                │
│  mysql-data     depends_on db   depends_on          │
│   volume       (healthy)        backend             │
└─────────────────────────────────────────────────────┘
```

**启动依赖链**:

```
db (MySQL)  →  healthcheck: mysqladmin ping  →  healthy
                        │
                        │ condition: service_healthy
                        ▼
                    backend (Spring Boot)
                        │
                        │ depends_on: backend
                        ▼
                    frontend (Nginx)
```

**关键设计**:
- MySQL 使用 `healthcheck` 确保就绪后 Backend 才启动
- MySQL 数据持久化到 `mysql-data` 命名卷
- 所有服务在同一 `activities-network` 桥接网络中通信
- Backend 通过环境变量覆盖数据库连接配置

### 7.2 Nginx 配置

**源文件**: `nginx.conf`

| 路径 | 处理方式 | 说明 |
|------|----------|------|
| `/` | 静态资源托管 | `try_files $uri $uri/ /index.html` 支持 SPA History 模式路由 |
| `/api` | 反向代理 | `proxy_pass http://backend:8080`，透传真实 IP 头信息 |

---

## 八、数据流总结

### 完整签到流程数据流

```
管理员创建活动                参与者签到
     │                          │
     ▼                          ▼
POST /api/admin/activities    GET /api/public/activities/{token}
     │                          │
     ▼                          ▼
ActivityService               ActivityService
  .createActivity()             .getActivityByToken()
     │                          │
     ▼                          ▼
MySQL INSERT activity          MySQL SELECT activity
  (status=DRAFT)                (WHERE checkin_token=?)
     │                          │
     ▼                          ▼
管理员发布活动                校验 status=PUBLISHED
     │                          │
     ▼                          ▼
POST /api/admin/activities   POST /api/public/activities/{token}/checkin
     /{id}/publish               │
     │                          ▼
     ▼                       CheckinService
ActivityService               .checkin()
  .publishActivity()            │
  生成 UUID token               ▼
  status→PUBLISHED           MySQL INSERT checkin_record
     │                          │
     ▼                          ▼
MySQL UPDATE activity         签到成功 → 跳转成功页
  (status=PUBLISHED,
   checkin_token=UUID)
```
