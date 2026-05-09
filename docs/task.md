# 文体活动管理平台 - 任务清单

## 📋 项目概述
开发一个文体活动管理平台，支持活动发布、二维码签到、签到记录管理和导出功能。

---

## 🔧 阶段一：项目初始化
- [x] 创建项目目录结构（activities/）
- [x] 初始化后端项目（Spring Boot 2 + Java）
- [x] 初始化前端项目（Vue 3 + Vite）
- [x] 配置 .gitignore 和 .dockerignore
- [x] 创建基础 README.md

---

## 🗄️ 阶段二：数据库设计与实现
- [x] 设计数据库表结构（activity, checkin_record）
- [x] 创建 JPA Entity 实体类
  - [x] Activity 实体（id, title, startTime, endTime, description, status, checkinToken, createdAt）
  - [x] CheckinRecord 实体（id, activityId, name, checkinTime）
- [x] 创建 Repository 接口
- [x] 配置数据库连接和初始化脚本

---

## 🔌 阶段三：后端核心功能开发
### 3.1 管理端 API
- [x] POST /api/admin/activities - 创建活动
- [x] POST /api/admin/activities/{id}/publish - 发布活动（生成 token 和链接）
- [x] POST /api/admin/activities/{id}/close - 关闭活动
- [x] GET /api/admin/activities - 查询活动列表
- [x] GET /api/admin/activities/{id} - 查询活动详情
- [x] GET /api/admin/activities/{id}/checkins - 查看签到名单
- [x] GET /api/admin/activities/{id}/checkins/export - 导出签到名单（CSV）

### 3.2 参与者端 API
- [x] GET /api/public/activities/{token} - 获取活动信息
- [x] POST /api/public/activities/{token}/checkin - 提交签到

### 3.3 业务逻辑实现
- [x] 实现活动创建逻辑
- [x] 实现活动发布逻辑（生成唯一 token）
- [x] 实现活动关闭逻辑
- [x] 实现签到逻辑（姓名校验、状态校验）
- [x] 实现签到名单查询和统计
- [x] 实现 CSV 导出功能
- [x] 添加全局异常处理
- [x] 添加日志记录

---

## 🎨 阶段四：前端开发
### 4.1 管理端页面
- [x] 创建管理端主布局（使用 Element Plus / Ant Design Vue）
- [x] 活动列表页
  - [x] 显示活动列表（表格形式）
  - [x] 创建活动按钮和表单
  - [x] 发布活动功能
  - [x] 关闭活动功能
  - [x] 查看签到名单入口
- [x] 活动详情页
  - [x] 显示活动基本信息
  - [x] 显示二维码（使用 qrcode.js）
  - [x] 显示签到链接（可复制）
  - [x] 签到人数统计
  - [x] 签到名单列表
  - [x] 导出 CSV 按钮
- [x] 创建活动表单
  - [x] 标题输入框
  - [x] 活动时间选择（日期时间范围）
  - [x] 说明文本框（可选）
  - [x] 表单验证

### 4.2 参与者端页面
- [x] 创建签到页面
  - [x] 显示活动信息（标题、时间、说明）
  - [x] 姓名输入框（必填）
  - [x] 提交按钮
  - [x] 表单验证
  - [x] Loading 状态
- [x] 创建签到成功页
  - [x] 显示"已签到"提示
  - [x] 显示签到时间
  - [x] 成功动画效果

### 4.3 通用组件和功能
- [x] 配置 Axios 创建 API 请求封装
- [x] 设置 Toast 消息提示
- [x] 添加 Loading 加载状态组件
- [x] 添加 Error Boundary 错误边界
- [x] 配置路由（Vue Router）
- [x] 响应式布局适配（PC 端和移动端）
- [x] 修改浏览器标签页标题为“活动”
- [x] 更换浏览器标签页图标
- [x] 在二维码处添加“需正式上线可用”提示

---

## 🐳 阶段五：Docker 容器化
- [x] 创建后端 Dockerfile（多阶段构建）
- [x] 创建前端 Dockerfile（多阶段构建）
- [x] 创建 docker-compose.yml
  - [x] 配置 MySQL 服务（数据持久化）
  - [x] 配置后端服务
  - [x] 配置前端服务（Nginx）
  - [x] 配置网络和端口映射
- [x] 创建数据库初始化脚本（Seed 数据）
- [x] 配置 .dockerignore
- [x] 测试一键启动（docker compose up --build）

---

## 📝 阶段六：文档完善
- [x] 完善 README.md
  - [x] 技术栈说明
  - [x] 快速启动指南
  - [x] 服务地址列表
  - [x] 功能介绍和截图
  - [x] 技术亮点
  - [x] 项目结构
  - [x] 常见问题解答
- [x] 创建 API 文档
- [x] 添加代码注释

---

## ✅ 阶段七：测试与验证
- [x] 后端 API 测试
  - [x] 测试活动创建
  - [x] 测试活动发布（token 生成）
  - [x] 测试活动关闭
  - [x] 测试签到提交
  - [x] 测试签到名单查询
- [x] 前端功能测试
  - [x] 测试管理端所有页面
  - [x] 测试参与者端签到流程
  - [x] 测试二维码扫描
  - [x] 测试移动端适配
- [x] Docker 容器测试
  - [x] 测试容器启动
  - [x] 测试数据持久化
  - [x] 测试网络连通性
- [x] 验收自测清单检查

---

## 🎯 开发顺序建议
1. 后端：活动创建/发布（生成 token 链接）
2. 前端：管理端活动页展示二维码 + 链接
3. 前端：签到页（姓名输入 + 提交）
4. 后端：写入签到记录 + 管理端列表 + 导出
5. Docker 容器化
6. 测试和文档完善
