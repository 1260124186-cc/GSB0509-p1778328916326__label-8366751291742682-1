# 文体活动管理平台 (Activities Management Platform)

## 🛠 技术栈
- **Frontend**: Vue 3 + Vite + Element Plus
- **Backend**: Spring Boot 2 + Spring Data JPA
- **Database**: MySQL 8.0

## 🚀 快速启动 (Docker)

### 前置要求
- Docker Desktop 已安装并运行

### 启动步骤
1. 确保 Docker Desktop 已运行
2. 在项目根目录执行：
   ```bash
   docker compose up --build
   ```
3. 等待所有容器启动完成...

## 🔗 服务地址 (Services)
- **前端**: http://localhost:3000
- **后端 API**: http://localhost:8080/api
- **数据库**: localhost:3306 (user: root / pass: root)

## 📸 功能介绍
- **活动发布**: 管理员创建并发布活动，生成专属签到二维码。
- **扫码签到**: 参与者扫描二维码或访问链接完成签到。
- **数据管理**: 实时查看签到名单。

## 🛠 技术亮点
- ✅ 100% Docker 容器化
- ✅ 一键启动，零依赖
- ✅ 响应式设计，移动端适配
- ✅ 完整的错误处理和日志系统
- ✅ 数据持久化存储

## 📚 项目文档 (Documentation)
### 核心交付物
- [📘 用户手册 (User Manual)](docs/user_manual.md): 管理员和参与者的操作指南
- [🏗️ 系统架构 (Architecture)](docs/architecture.md): 系统整体架构图与技术栈说明
- [🎨 详细设计 (Detailed Design)](docs/design.md): 数据库设计与 API 接口定义
- [🛠️ 开发指南 (Development Guide)](docs/development.md): 本地开发环境搭建与运行方式
- [✅ 测试报告 (Testing Report)](docs/testing.md): 功能验证与测试通过情况汇总

### 过程文档
- [📋 任务清单 (Task List)](docs/task.md): 开发过程的全程追踪
- [📝 实施方案 (Implementation Plan)](docs/implementation_plan.md): 原始开发计划
- [👣 逻辑梳理 (Walkthrough)](docs/walkthrough.md): 关键功能开发记录

## 📦 项目结构
```
activities/
├── docker-compose.yml  # 容器编排
├── docs/              # 项目文档
├── frontend/          # Vue 3 前端
└── backend/           # Spring Boot 后端
```
