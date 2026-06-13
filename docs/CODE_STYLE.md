# Personal AI Platform - 代码开发规范

# 通用原则

所有代码必须遵循：

* SOLID
* Clean Code
* DDD
* KISS
* DRY

禁止：

* 重复代码
* 超长方法
* 超长类
* 魔法值
* 硬编码

要求：

* 高可读性
* 高可维护性
* 易扩展

---

# Java开发规范

## JDK版本

统一：

JDK 21

必须优先使用：

* Record
* Stream API
* Optional
* Virtual Thread

---

## 包结构

统一：

com.xxx.platform

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

config

constant

enums

exception

---

## Controller规范

Controller只负责：

* 参数接收
* 参数校验
* 调用Service
* 返回结果

禁止：

* SQL
* 业务逻辑
* 复杂计算

示例：

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {

```
private final ArticleService articleService;
```

}

---

## Service规范

业务逻辑必须放在Service。

要求：

* 单一职责
* 方法长度不超过50行
* 一个方法只做一件事

禁止：

* Controller处理业务
* Mapper直接被Controller调用

---

## Entity规范

Entity仅用于数据库映射。

禁止：

* 返回前端
* 接口参数

---

## DTO规范

DTO用于：

请求参数

命名：

CreateArticleDTO

UpdateArticleDTO

ArticleQueryDTO

---

## VO规范

VO用于：

接口返回

命名：

ArticleVO

ArticleDetailVO

PageArticleVO

---

## Convert规范

统一使用MapStruct。

禁止：

BeanUtils.copyProperties

示例：

@Mapper(componentModel = "spring")
public interface ArticleConvert {

}

---

## Mapper规范

统一使用：

MyBatis Plus

禁止：

XML

优先：

LambdaQueryWrapper

LambdaUpdateWrapper

---

## 异常处理

统一：

GlobalExceptionHandler

禁止：

try catch满天飞

业务异常：

BusinessException

---

## 返回结构

统一：

Result<T>

格式：

{
"code": 200,
"message": "success",
"data": {}
}

---

# 数据库规范

## 命名规范

表名：

小写

下划线

示例：

sys_user

biz_article

kb_document

---

## 字段规范

主键：

id

统一审计字段：

create_by

create_time

update_by

update_time

deleted

---

## 主键

统一：

Snowflake

禁止：

AUTO_INCREMENT

---

## 索引规范

必须：

主键索引

唯一索引

查询索引

禁止：

无索引模糊查询

---

# Spring规范

## 注入方式

统一：

构造器注入

@RequiredArgsConstructor

禁止：

@Autowired字段注入

---

## 配置

统一：

@ConfigurationProperties

禁止：

@Value大量使用

---

## 日志规范

统一：

@Slf4j

日志格式：

log.info("创建文章成功，articleId={}", articleId);

禁止：

字符串拼接日志

---

# API规范

## URL规范

使用：

/api/articles

/api/articles/{id}

禁止：

/getArticle

/deleteArticle

---

## HTTP规范

GET

查询

POST

新增

PUT

修改

DELETE

删除

---

# Vue3规范

## 统一使用

Composition API

script setup

TypeScript

---

## 目录结构

src

api

modules

components

stores

hooks

utils

router

layouts

---

## 页面目录

modules

blog

article

category

tag

comment

---

# Vue组件规范

组件名称：

PascalCase

示例：

ArticleForm.vue

ArticleList.vue

ArticleDetail.vue

---

# Pinia规范

每个业务独立Store。

示例：

useUserStore

useArticleStore

---

# API规范

统一封装：

src/api

示例：

article.ts

user.ts

auth.ts

禁止：

页面直接axios请求

---

# Hooks规范

复用逻辑必须抽离。

示例：

usePagination

useTable

useUser

---

# TypeScript规范

禁止：

any

必须：

interface

type

---

# 样式规范

统一：

TailwindCSS

禁止：

大量内联style

---

# AI开发规范

所有AI能力统一通过：

AiService

调用。

禁止：

业务模块直接调用模型SDK。

---

# 代码生成要求

AI生成代码时必须包含：

Entity

DTO

VO

Mapper

Convert

Service

Controller

SQL

前端页面

API定义

禁止：

TODO

自行补充

伪代码

省略实现

所有代码必须达到可运行标准。
