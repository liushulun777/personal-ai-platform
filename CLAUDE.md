# Personal AI Platform

## 项目简介

Personal AI Platform 是一个长期演进的个人技术中台项目。

博客系统仅为第一阶段业务模块。

项目最终目标：

* 技术博客平台
* AI知识库
* AI助手
* 项目管理平台
* MCP工具平台
* Agent工作流平台

当前开发阶段：

Phase1：博客系统

未来扩展：

Phase2：知识库

Phase3：项目管理

Phase4：AI助手

Phase5：MCP平台

Phase6：Agent工作流

---

# 技术栈

## 后端

* JDK 21
* Spring Boot 3.5+
* Spring Cloud 2025
* Spring AI
* Spring Security
* MyBatis Plus
* PostgreSQL 17
* Redis 8
* Kafka
* ElasticSearch
* MinIO
* OpenTelemetry

## 前端

* Vue3
* TypeScript
* Vite
* Pinia
* Vue Router
* Naive UI
* TailwindCSS
* TipTap Editor
* TanStack Query

---

# 架构原则

必须遵守：

* SOLID
* Clean Architecture
* DDD
* 低耦合
* 高内聚
* 模块化设计

禁止：

* 巨型Service
* 巨型Controller
* 工具类堆积
* XML配置
* Field注入

统一：

* Constructor注入
* Lombok RequiredArgsConstructor
* RESTful API
* DTO/VO分离
* 统一响应结构

---

# 微服务规划

当前：

gateway-service

auth-service

system-service

blog-service

file-service

search-service

ai-service

未来：

knowledge-service

project-service

workflow-service

mcp-service

message-service

---

# Maven结构

platform-parent

common

common-core

common-security

common-web

common-ai

services

gateway-service

auth-service

system-service

blog-service

file-service

search-service

ai-service

frontend

platform-web

---

# 数据库设计原则

系统表：

sys_user

sys_role

sys_menu

sys_dict

sys_log

业务表：

biz_article

biz_category

biz_tag

biz_comment

biz_favorite

biz_like

知识库：

kb_document

kb_chunk

kb_embedding

项目管理：

pm_project

pm_task

pm_bug

AI模块：

ai_conversation

ai_message

ai_prompt

禁止使用无意义表名。

---

# 统一审计字段

所有表必须包含：

id

create_by

create_time

update_by

update_time

deleted

说明：

* deleted 为逻辑删除
* 使用 MyBatis Plus 逻辑删除

---

# 主键策略

统一：

Snowflake

禁止：

AUTO_INCREMENT

---

# Java规范

必须使用：

* record
* Stream
* Optional
* Virtual Thread

统一包结构：

controller

service

service.impl

domain.entity

domain.dto

domain.vo

mapper

convert

event

listener

禁止：

entity直接返回前端

---

# API返回格式

{
"code": 200,
"message": "success",
"data": {}
}

分页：

{
"records": [],
"total": 0,
"current": 1,
"size": 10
}

---

# 前端规范

统一使用：

Composition API

script setup

TypeScript

禁止：

Options API

统一目录：

src/modules

src/api

src/stores

src/components

src/layouts

src/hooks

src/utils

---

# 权限设计

RBAC模型：

用户

角色

菜单

权限

数据权限

后续支持：

Github OAuth

OIDC

SSO

---

# 文件管理

统一由 file-service 管理。

禁止业务模块直接处理文件。

存储：

MinIO

支持：

图片

Markdown

PDF

Word

附件

---

# 搜索设计

统一由 search-service 管理。

技术：

ElasticSearch

文章发布：

ArticlePublishEvent

↓

Kafka

↓

SearchService

↓

ElasticSearch

禁止直接 MySQL Like 查询。

---

# AI设计

统一由 ai-service 管理。

定义接口：

AiService

支持：

chat

summary

generateTags

generateTitle

未来支持：

RAG

Agent

Workflow

MCP

业务模块禁止直接调用大模型SDK。

必须通过 AiService。

---

# 博客模块

当前优先开发模块。

功能：

文章管理

分类管理

标签管理

评论管理

点赞管理

收藏管理

文章搜索

AI摘要

AI标签生成

AI标题生成

---

# 文章实体

字段：

id

title

summary

content

cover

authorId

categoryId

status

viewCount

likeCount

favoriteCount

commentCount

publishTime

createTime

updateTime

状态：

DRAFT

PUBLISHED

ARCHIVED

---

# 开发顺序

Step1

基础工程

Step2

用户模块

Step3

权限模块

Step4

博客模块

Step5

文件模块

Step6

搜索模块

Step7

AI模块

Step8

Docker部署

Step9

Kubernetes部署

---

# AI生成要求

生成代码时必须：

1. 输出完整代码
2. 不省略实现
3. 不使用伪代码
4. 提供DTO
5. 提供VO
6. 提供Mapper
7. 提供SQL
8. 提供接口文档
9. 提供单元测试
10. 保证可运行

禁止：

TODO

自行补充

省略实现

仅展示核心代码

所有生成代码必须达到生产可运行标准。
