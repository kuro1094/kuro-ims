# Kuro IMS · 智能车间监控系统

基于 **Vue 3 + Spring Boot + Three.js + MySQL** 的 3D 仓储园区可视化平台。
借鉴策略游戏地图交互：在 3D 园区中点击仓库查看运营、点击货车追踪运输、点击叉车/托盘查看任务与货物，空间位置与业务数据同屏关联。

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3 · Vite · Pinia · Vue Router · Three.js · Axios |
| 后端 | Spring Boot 3 · Spring Security · MyBatis-Plus · JWT |
| 数据库 | MySQL 8 |

## 功能

- **用户体系**：注册 / 登录 / JWT 双 Token（Access 2h + Refresh 7d）/ 过期自动刷新
- **3D 园区**：仓库、货车、叉车、货柜、托盘三维可视化，悬停高亮 + 点击查看业务详情，在途车辆/作业叉车实时动画
- **运营面板**：统计总览、任务列表、设备监控（OEE/温湿度）、车辆追踪
- **货柜计算**：支持 20GP/40GP/40HQ/45HQ 标准柜与自定义柜，按容积 + 载重计算装载率与未装入明细
- **统一视觉**：透明毛玻璃主题，统一色板 / 字号 / 圆角体系

## 目录结构

```
kuro-ims/
├── backend/                 # Spring Boot 后端
│   └── src/main/
│       ├── java/com/kuro/ims/
│       │   ├── config/      # Security、MyBatis-Plus、CORS 配置
│       │   ├── controller/  # REST API（8 个资源 + 货柜计算 + 统计）
│       │   ├── dto/         # 请求/响应对象
│       │   ├── entity/      # 8 张表实体
│       │   ├── filter/      # JWT 过滤器
│       │   ├── mapper/      # MyBatis-Plus Mapper
│       │   ├── service/     # 认证、货柜计算
│       │   └── utils/       # JwtUtils、UserContext
│       └── resources/
│           ├── application.yml
│           └── sql/init.sql # 建库建表 + 演示数据
└── frontend/                # Vue 3 前端
    └── src/
        ├── api/             # Axios 封装（自动刷新 Token）
        ├── components/      # Scene3D / SidePanel / DetailPanel / ContainerCalc
        ├── store/           # Pinia 认证状态
        ├── styles/          # 全局毛玻璃主题
        └── views/           # 登录页 / 主驾驶舱
```

## 快速启动

### 1. 初始化数据库

```bash
mysql -uroot -p<你的密码> < backend/src/main/resources/sql/init.sql
```

默认演示账号：**admin / 123456**

### 2. 启动后端（端口 8080）

```bash
cd backend
# 数据库密码不是 123456 时通过环境变量覆盖
MYSQL_PASSWORD=你的密码 mvn spring-boot:run
```

### 3. 启动前端（端口 5173）

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173 ，Vite 已配置 `/api` 代理到后端。

## 配置项（backend/src/main/resources/application.yml）

| 配置 | 默认 | 说明 |
|---|---|---|
| `MYSQL_HOST` `MYSQL_PORT` `MYSQL_DB` | localhost 3306 kuro_ims | 数据库连接 |
| `MYSQL_USER` `MYSQL_PASSWORD` | root 123456 | 数据库账号 |
| `jwt.secret` | 内置开发密钥 | 生产环境务必覆盖 |
| `jwt.expire` | 7200000 | Access Token 有效期（2h） |
| `jwt.refresh-expire` | 604800000 | Refresh Token 有效期（7d） |

## API 概览

- `POST /api/auth/login` `POST /api/auth/register` `POST /api/auth/refresh` `GET /api/auth/profile`
- `GET|POST|PUT|DELETE /api/warehouses` （containers / goods / vehicles / forklifts / tasks / devices 同构）
- `POST /api/containers/calculate` 货柜装载计算
- `GET /api/stats/overview` 运营总览统计
