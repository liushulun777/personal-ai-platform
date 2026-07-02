# 报表平台需求文档 (PRD)

## 1. 项目概述

### 1.1 项目背景
基于现有的Personal AI Platform微服务架构，搭建一个类似FineReport、SmartBI的报表平台，支持数据可视化、大屏展示、自助分析等功能。

### 1.2 项目目标
- 提供拖拽式报表设计器，降低报表开发门槛
- 支持SQL查询和可视化配置两种报表开发模式
- 实现大屏可视化展示，支持丰富的图表和样式
- 支持多种数据源接入（MySQL/PostgreSQL、API、Excel/CSV）

### 1.3 技术栈
| 层级 | 技术选型 |
|------|----------|
| 后端 | Spring Boot 3.4 + Spring Cloud 2024 (report-service) |
| 前端 | Vue 3 + TypeScript + Naive UI + TailwindCSS (report-web) |
| 数据库 | PostgreSQL 16 |
| 缓存 | Redis |
| 图表库 | ECharts 5 |
| 报表引擎 | 自研 / Luckysheet (前端Excel引擎) |

---

## 2. 用户角色

| 角色 | 描述 | 权限 |
|------|------|------|
| 系统管理员 | 系统配置、用户管理 | 全部权限 |
| 数据管理员 | 数据源管理、数据集配置 | 数据源管理、数据集管理 |
| 报表开发者 | 设计和开发报表 | 报表设计、预览、发布 |
| 业务用户 | 查看和使用报表 | 报表查看、导出、打印 |

---

## 3. 功能模块

### 3.1 数据源管理

#### 3.1.1 数据源连接
- **功能描述**：管理外部数据源连接配置
- **支持类型**：
  - 关系型数据库：MySQL、PostgreSQL
  - API接口：RESTful API
  - 文件：Excel、CSV
- **操作功能**：
  - 新增数据源连接
  - 编辑数据源连接
  - 删除数据源连接
  - 测试连接
  - 连接池配置

#### 3.1.2 数据源配置字段
```yaml
数据源:
  - id: 数据源ID
  - name: 数据源名称
  - type: 数据源类型 (MYSQL/POSTGRESQL/API/EXCEL/CSV)
  - config: 连接配置 (JSON)
    # 关系型数据库
    - host: 主机地址
    - port: 端口
    - database: 数据库名
    - username: 用户名
    - password: 密码
    - poolSize: 连接池大小
    # API接口
    - url: API地址
    - method: 请求方式
    - headers: 请求头
    - auth: 认证配置
    # 文件
    - filePath: 文件路径
    - sheetName: Sheet名称 (Excel)
  - status: 状态 (启用/禁用)
  - createTime: 创建时间
  - updateTime: 更新时间
```

### 3.2 数据集管理

#### 3.2.1 SQL数据集
- **功能描述**：通过SQL语句定义数据集
- **核心功能**：
  - SQL编辑器（语法高亮、自动补全）
  - SQL预览（查看前100条数据）
  - 参数定义（支持动态参数）
  - 字段管理（自动识别字段类型）
  - 数据预览

#### 3.2.2 可视化数据集
- **功能描述**：通过拖拽方式配置数据集
- **核心功能**：
  - 选择数据源
  - 选择表/视图
  - 拖拽字段
  - 配置过滤条件
  - 配置聚合方式
  - 配置排序

#### 3.2.3 API数据集
- **功能描述**：通过API接口获取数据
- **核心功能**：
  - 配置API地址
  - 配置请求参数
  - 配置响应映射
  - 数据转换（JSONPath）

#### 3.2.4 数据集配置字段
```yaml
数据集:
  - id: 数据集ID
  - name: 数据集名称
  - type: 数据集类型 (SQL/VISUAL/API)
  - sourceId: 数据源ID
  - config: 配置信息 (JSON)
    # SQL数据集
    - sql: SQL语句
    - params: 参数列表
      - name: 参数名
      - type: 参数类型
      - defaultValue: 默认值
      - required: 是否必填
    # 可视化数据集
    - tables: 表列表
    - fields: 字段列表
    - filters: 过滤条件
    - groups: 分组字段
    - orders: 排序字段
    # API数据集
    - url: API地址
    - method: 请求方式
    - params: 请求参数
    - responseMapping: 响应映射
  - fields: 字段列表
    - name: 字段名
    - label: 字段标签
    - type: 字段类型 (STRING/NUMBER/DATE/BOOLEAN)
  - status: 状态
  - createTime: 创建时间
  - updateTime: 更新时间
```

### 3.3 报表设计器

#### 3.3.1 设计器概述
- **功能描述**：拖拽式报表设计器，支持复杂报表布局
- **设计模式**：
  - 普通报表：表格、列表、分组报表
  - 复杂报表：交叉报表、主子报表、分栏报表
  - 大屏报表：数据大屏、可视化大屏

#### 3.3.2 设计器功能

##### 3.3.2.1 画布管理
- 画布尺寸设置（A4、A3、自定义）
- 画布缩放（50%-200%）
- 画布背景设置
- 网格线显示/隐藏
- 标尺显示/隐藏

##### 3.3.2.2 组件库
**基础组件**：
- 文本框：静态文本、动态文本
- 图片：静态图片、动态图片
- 形状：矩形、圆形、线条
- 按钮：查询按钮、重置按钮、导出按钮

**数据组件**：
- 表格：普通表格、交叉表格
- 图表：柱状图、折线图、饼图、散点图、雷达图、热力图、地图等
- 指标卡：数字指标、趋势指标
- 列表：数据列表、分页列表

**布局组件**：
- 容器：分组容器、选项卡容器
- 分隔：水平分隔、垂直分隔
- 网格：栅格布局

**交互组件**：
- 参数面板：日期选择、下拉选择、文本输入、复选框
- 导航：面包屑、目录树

##### 3.3.2.3 数据绑定
- 字段拖拽绑定
- 动态表达式
- 聚合函数（SUM、AVG、COUNT、MAX、MIN）
- 条件格式
- 数据过滤

##### 3.3.2.4 样式设置
- 字体设置（字体、大小、颜色、加粗、斜体、下划线）
- 对齐设置（水平、垂直）
- 边框设置（样式、颜色、宽度）
- 背景设置（颜色、图片）
- 间距设置（内边距、外边距）

##### 3.3.2.5 布局功能
- 对齐（左、右、上、下、水平居中、垂直居中）
- 分布（水平分布、垂直分布）
- 层级（上移、下移、置顶、置底）
- 锁定/解锁
- 组合/取消组合
- 复制/粘贴/删除

#### 3.3.3 报表配置字段
```yaml
报表:
  - id: 报表ID
  - name: 报表名称
  - code: 报表编码
  - type: 报表类型 (NORMAL/COMPLEX/DASHBOARD)
  - category: 报表分类
  - config: 报表配置 (JSON)
    - canvas: 画布配置
      - width: 宽度
      - height: 高度
      - background: 背景
    - components: 组件列表
      - id: 组件ID
      - type: 组件类型
      - position: 位置
        - x: X坐标
        - y: Y坐标
        - width: 宽度
        - height: 高度
      - style: 样式配置
      - data: 数据绑定
        - datasetId: 数据集ID
        - fields: 字段绑定
        - filters: 过滤条件
        - sorts: 排序
      - interaction: 交互配置
    - params: 参数列表
    - layout: 布局配置
  - status: 状态 (草稿/已发布/已归档)
  - version: 版本号
  - creatorId: 创建者ID
  - createTime: 创建时间
  - updateTime: 更新时间
```

### 3.4 报表展示

#### 3.4.1 报表预览
- **功能描述**：预览报表效果
- **核心功能**：
  - 实时预览
  - 参数查询
  - 数据刷新
  - 全屏预览

#### 3.4.2 报表查看
- **功能描述**：查看已发布的报表
- **核心功能**：
  - 参数查询
  - 数据筛选
  - 排序
  - 分页
  - 钻取（下钻、上钻）
  - 联动（图表联动、报表联动）

#### 3.4.3 报表导出
- **功能描述**：导出报表数据
- **支持格式**：
  - Excel（.xlsx）
  - PDF
  - Word（.docx）
  - 图片（PNG、JPG）
  - CSV

#### 3.4.4 报表打印
- **功能描述**：打印报表
- **核心功能**：
  - 打印预览
  - 打印设置（纸张大小、方向、边距）
  - 批量打印
  - 套打模板

### 3.5 数据大屏

#### 3.5.1 大屏设计器
- **功能描述**：数据大屏设计器
- **核心功能**：
  - 全屏画布
  - 组件拖拽
  - 图表配置
  - 数据绑定
  - 样式设置
  - 动画效果

#### 3.5.2 大屏组件
**图表组件**：
- 柱状图（基础、堆叠、分组）
- 折线图（基础、面积、堆叠）
- 饼图（基础、环形、玫瑰）
- 散点图
- 雷达图
- 热力图
- 地图（中国地图、世界地图、区域地图）
- 仪表盘
- 漏斗图
- 桑基图
- 树图
- 旭日图

**装饰组件**：
- 边框装饰
- 图片装饰
- 文字装饰
- 数字翻牌器
- 轮播表
- 滚动表格
- 词云

**交互组件**：
- Tab切换
- 轮播
- 定时刷新

#### 3.5.3 大屏配置
```yaml
大屏:
  - id: 大屏ID
  - name: 大屏名称
  - config: 大屏配置 (JSON)
    - canvas: 画布配置
      - width: 宽度 (默认1920)
      - height: 高度 (默认1080)
      - background: 背景（颜色/图片）
    - components: 组件列表
    - theme: 主题配置
    - animation: 动画配置
  - status: 状态
  - shareUrl: 分享链接
  - createTime: 创建时间
```

### 3.6 定时调度

#### 3.6.1 定时任务
- **功能描述**：定时生成和发送报表
- **核心功能**：
  - 定时执行（Cron表达式）
  - 邮件发送
  - 消息通知
  - 任务监控

#### 3.6.2 调度配置
```yaml
调度任务:
  - id: 任务ID
  - name: 任务名称
  - reportId: 报表ID
  - cron: Cron表达式
  - config: 执行配置 (JSON)
    - exportFormat: 导出格式
    - params: 参数值
    - receivers: 接收人列表
    - emailConfig: 邮件配置
      - subject: 邮件主题
      - content: 邮件内容
      - attachments: 附件
  - status: 状态 (运行中/已暂停/已停止)
  - lastExecuteTime: 上次执行时间
  - nextExecuteTime: 下次执行时间
```

### 3.7 权限管理

#### 3.7.1 功能权限
- 数据源管理权限
- 数据集管理权限
- 报表设计权限
- 报表查看权限
- 大屏设计权限
- 大屏查看权限
- 定时调度权限

#### 3.7.2 数据权限
- 行级权限：按条件过滤数据
- 列级权限：控制字段可见性
- 数据源权限：控制可访问的数据源

#### 3.7.3 权限配置
```yaml
报表权限:
  - id: 权限ID
  - userId: 用户ID
  - roleId: 角色ID
  - resourceId: 资源ID
  - resourceType: 资源类型 (DATASOURCE/DATASET/REPORT/DASHBOARD)
  - permission: 权限类型 (VIEW/EDIT/DELETE/EXPORT/PRINT)
  - dataScope: 数据范围 (JSON)
    - type: 范围类型 (ALL/DEPT/SELF/CUSTOM)
    - rules: 规则列表
```

---

## 4. 非功能需求

### 4.1 性能需求
- 报表查询响应时间：< 3秒（常规查询）
- 大数据量报表：支持百万级数据展示
- 并发用户：支持100+并发用户
- 大屏刷新：支持秒级数据刷新

### 4.2 安全需求
- SQL注入防护
- XSS防护
- CSRF防护
- 数据加密传输
- 敏感数据脱敏
- 操作日志审计

### 4.3 可用性需求
- 系统可用性：99.9%
- 数据备份：每日自动备份
- 故障恢复：RTO < 4小时，RPO < 1小时

### 4.4 兼容性需求
- 浏览器：Chrome 90+、Firefox 88+、Edge 90+
- 分辨率：支持1920x1080及以上分辨率
- 移动端：响应式布局（查看功能）

---

## 5. 系统架构

### 5.1 整体架构
```
┌─────────────────────────────────────────────────────────────┐
│                        前端层                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │  report-web  │  │ platform-web│  │  mobile-web │          │
│  │  (报表前端)   │  │ (平台前端)  │  │  (移动端)   │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      网关层                                  │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                 gateway-service                      │    │
│  │            (路由、限流、认证、日志)                     │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      服务层                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │report-service│  │ auth-service│  │system-service│         │
│  │  (报表服务)  │  │  (认证服务) │  │  (系统服务)  │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ ai-service  │  │blog-service │  │其他服务...   │          │
│  │  (AI服务)   │  │  (博客服务) │  │             │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据层                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ PostgreSQL  │  │    Redis    │  │    MinIO    │          │
│  │  (主数据库)  │  │   (缓存)    │  │  (文件存储)  │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 report-service架构
```
report-service
├── src/main/java/com/platform/report/
│   ├── ReportApplication.java          # 启动类
│   ├── config/                         # 配置类
│   │   ├── DataSourceConfig.java       # 数据源配置
│   │   ├── CacheConfig.java           # 缓存配置
│   │   └── AsyncConfig.java           # 异步配置
│   ├── controller/                     # 控制器
│   │   ├── DataSourceController.java   # 数据源管理
│   │   ├── DataSetController.java      # 数据集管理
│   │   ├── ReportController.java       # 报表管理
│   │   ├── DashboardController.java    # 大屏管理
│   │   └── ScheduleController.java     # 调度管理
│   ├── service/                        # 服务层
│   │   ├── DataSourceService.java
│   │   ├── DataSetService.java
│   │   ├── ReportService.java
│   │   ├── DashboardService.java
│   │   ├── ScheduleService.java
│   │   └── engine/                     # 报表引擎
│   │       ├── ReportEngine.java       # 报表引擎接口
│   │       ├── SqlEngine.java          # SQL引擎
│   │       ├── ExportEngine.java       # 导出引擎
│   │       └── CacheEngine.java        # 缓存引擎
│   ├── domain/                         # 领域模型
│   │   ├── entity/                     # 实体类
│   │   ├── dto/                        # DTO
│   │   └── vo/                         # VO
│   ├── mapper/                         # Mapper接口
│   └── client/                         # Feign客户端
│       ├── AuthServiceClient.java
│       └── SystemServiceClient.java
└── src/main/resources/
    ├── application.yml                 # 配置文件
    └── mapper/                         # MyBatis映射文件
```

### 5.3 report-web架构
```
report-web
├── src/
│   ├── main.ts                         # 入口文件
│   ├── App.vue                         # 根组件
│   ├── router/                         # 路由配置
│   │   └── index.ts
│   ├── stores/                         # Pinia状态管理
│   │   ├── datasource.ts               # 数据源状态
│   │   ├── dataset.ts                  # 数据集状态
│   │   ├── report.ts                   # 报表状态
│   │   └── dashboard.ts               # 大屏状态
│   ├── api/                            # API接口
│   │   ├── datasource.ts
│   │   ├── dataset.ts
│   │   ├── report.ts
│   │   └── dashboard.ts
│   ├── views/                          # 页面视图
│   │   ├── datasource/                 # 数据源管理
│   │   ├── dataset/                    # 数据集管理
│   │   ├── report/                     # 报表管理
│   │   │   ├── designer/              # 报表设计器
│   │   │   │   ├── index.vue
│   │   │   │   ├── Canvas.vue         # 画布组件
│   │   │   │   ├── Toolbar.vue        # 工具栏
│   │   │   │   ├── ComponentPanel.vue # 组件面板
│   │   │   │   ├── PropertyPanel.vue  # 属性面板
│   │   │   │   └── components/        # 报表组件
│   │   │   ├── preview/               # 报表预览
│   │   │   └── list/                  # 报表列表
│   │   ├── dashboard/                  # 大屏管理
│   │   │   ├── designer/              # 大屏设计器
│   │   │   ├── preview/               # 大屏预览
│   │   │   └── list/                  # 大屏列表
│   │   └── schedule/                   # 调度管理
│   ├── components/                     # 公共组件
│   │   ├── charts/                    # 图表组件
│   │   ├── widgets/                   # 控件组件
│   │   └── common/                    # 通用组件
│   ├── utils/                          # 工具函数
│   │   ├── echarts.ts                 # ECharts配置
│   │   ├── excel.ts                   # Excel处理
│   │   └── print.ts                   # 打印处理
│   └── styles/                         # 样式文件
│       ├── variables.scss
│       └── global.scss
├── package.json
├── vite.config.ts
└── tsconfig.json
```

---

## 6. 数据库设计

### 6.1 核心表结构

#### 6.1.1 数据源表 (report_datasource)
```sql
CREATE TABLE report_datasource (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,  -- MYSQL/POSTGRESQL/API/EXCEL/CSV
    config JSONB NOT NULL,
    status SMALLINT DEFAULT 1,
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 6.1.2 数据集表 (report_dataset)
```sql
CREATE TABLE report_dataset (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,  -- SQL/VISUAL/API
    source_id BIGINT NOT NULL,
    config JSONB NOT NULL,
    fields JSONB,
    status SMALLINT DEFAULT 1,
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 6.1.3 报表表 (report_definition)
```sql
CREATE TABLE report_definition (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    type VARCHAR(20) NOT NULL,  -- NORMAL/COMPLEX/DASHBOARD
    category VARCHAR(50),
    config JSONB NOT NULL,
    status SMALLINT DEFAULT 0,  -- 0:草稿 1:已发布 2:已归档
    version INTEGER DEFAULT 1,
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 6.1.4 大屏表 (report_dashboard)
```sql
CREATE TABLE report_dashboard (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    config JSONB NOT NULL,
    status SMALLINT DEFAULT 0,
    share_url VARCHAR(200),
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 6.1.5 调度任务表 (report_schedule)
```sql
CREATE TABLE report_schedule (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    report_id BIGINT NOT NULL,
    cron VARCHAR(50) NOT NULL,
    config JSONB NOT NULL,
    status SMALLINT DEFAULT 0,  -- 0:停止 1:运行 2:暂停
    last_execute_time TIMESTAMP,
    next_execute_time TIMESTAMP,
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 6.1.6 报表权限表 (report_permission)
```sql
CREATE TABLE report_permission (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    role_id BIGINT,
    resource_id BIGINT NOT NULL,
    resource_type VARCHAR(20) NOT NULL,  -- DATASOURCE/DATASET/REPORT/DASHBOARD
    permission VARCHAR(20) NOT NULL,     -- VIEW/EDIT/DELETE/EXPORT/PRINT
    data_scope JSONB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 6.1.7 操作日志表 (report_log)
```sql
CREATE TABLE report_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    resource_id BIGINT,
    resource_type VARCHAR(20),
    action VARCHAR(20),  -- CREATE/UPDATE/DELETE/VIEW/EXPORT/PRINT
    detail JSONB,
    ip_address VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 7. API接口设计

### 7.1 数据源管理接口
```
GET    /api/report/datasources          # 分页查询数据源
GET    /api/report/datasources/{id}      # 获取数据源详情
POST   /api/report/datasources          # 创建数据源
PUT    /api/report/datasources/{id}      # 更新数据源
DELETE /api/report/datasources/{id}      # 删除数据源
POST   /api/report/datasources/test     # 测试数据源连接
```

### 7.2 数据集管理接口
```
GET    /api/report/datasets              # 分页查询数据集
GET    /api/report/datasets/{id}         # 获取数据集详情
POST   /api/report/datasets              # 创建数据集
PUT    /api/report/datasets/{id}         # 更新数据集
DELETE /api/report/datasets/{id}         # 删除数据集
POST   /api/report/datasets/preview     # 预览数据集数据
GET    /api/report/datasets/{id}/fields  # 获取数据集字段
```

### 7.3 报表管理接口
```
GET    /api/report/reports               # 分页查询报表
GET    /api/report/reports/{id}          # 获取报表详情
POST   /api/report/reports               # 创建报表
PUT    /api/report/reports/{id}          # 更新报表
DELETE /api/report/reports/{id}          # 删除报表
POST   /api/report/reports/{id}/publish # 发布报表
POST   /api/report/reports/{id}/archive # 归档报表
GET    /api/report/reports/{id}/data    # 获取报表数据
POST   /api/report/reports/{id}/export  # 导出报表
POST   /api/report/reports/{id}/print   # 打印报表
```

### 7.4 大屏管理接口
```
GET    /api/report/dashboards            # 分页查询大屏
GET    /api/report/dashboards/{id}       # 获取大屏详情
POST   /api/report/dashboards            # 创建大屏
PUT    /api/report/dashboards/{id}       # 更新大屏
DELETE /api/report/dashboards/{id}       # 删除大屏
POST   /api/report/dashboards/{id}/share # 分享大屏
GET    /api/report/dashboards/{id}/data  # 获取大屏数据
```

### 7.5 调度管理接口
```
GET    /api/report/schedules             # 分页查询调度任务
GET    /api/report/schedules/{id}        # 获取调度任务详情
POST   /api/report/schedules             # 创建调度任务
PUT    /api/report/schedules/{id}        # 更新调度任务
DELETE /api/report/schedules/{id}        # 删除调度任务
POST   /api/report/schedules/{id}/start # 启动调度任务
POST   /api/report/schedules/{id}/stop  # 停止调度任务
POST   /api/report/schedules/{id}/pause # 暂停调度任务
```

### 7.6 权限管理接口
```
GET    /api/report/permissions           # 查询权限列表
POST   /api/report/permissions           # 创建权限
PUT    /api/report/permissions/{id}      # 更新权限
DELETE /api/report/permissions/{id}      # 删除权限
GET    /api/report/permissions/check    # 检查权限
```

---

## 8. 开发计划

### 8.1 阶段一：基础功能（4周）
- [ ] 数据源管理模块
- [ ] 数据集管理模块
- [ ] 报表设计器基础功能
- [ ] 报表预览和查看

### 8.2 阶段二：高级功能（4周）
- [ ] 复杂报表支持
- [ ] 图表组件丰富
- [ ] 报表导出和打印
- [ ] 参数查询和钻取

### 8.3 阶段三：大屏功能（3周）
- [ ] 大屏设计器
- [ ] 大屏组件库
- [ ] 大屏预览和分享
- [ ] 动画效果

### 8.4 阶段四：企业功能（3周）
- [ ] 定时调度
- [ ] 权限管理
- [ ] 操作日志
- [ ] 性能优化

---

## 9. 附录

### 9.1 术语表
| 术语 | 说明 |
|------|------|
| 数据源 | 外部数据来源，如数据库、API、文件 |
| 数据集 | 从数据源获取的数据集合 |
| 报表 | 数据展示的模板，包含布局和样式 |
| 大屏 | 大屏可视化展示页面 |
| 组件 | 报表/大屏中的可视化元素 |
| 钻取 | 从汇总数据查看明细数据 |
| 联动 | 多个组件之间的数据关联 |

### 9.2 参考资料
- [FineReport官方文档](https://www.finereport.com/)
- [SmartBI官方文档](https://www.smartbi.com.cn/)
- [ECharts官方文档](https://echarts.apache.org/)
- [Luckysheet官方文档](https://mengshukeji.github.io/LuckysheetDocs/)


