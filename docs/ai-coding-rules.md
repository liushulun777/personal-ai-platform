# AI 编码规范

> 本规范适用于 Personal AI Platform 项目中的所有代码生成和开发工作。

---

## 1. 项目结构规范

### 1.1 包结构

```
com.platform.{service-name}
├── controller/          # 控制器层
├── service/             # 服务接口
│   └── impl/           # 服务实现
├── mapper/              # MyBatis-Plus Mapper
├── domain/
│   ├── entity/          # 实体类（对应数据库表）
│   ├── dto/             # 数据传输对象（请求参数）
│   └── vo/              # 视图对象（响应数据）
├── convert/             # MapStruct 转换器
├── config/              # 配置类
└── {ServiceName}Application.java  # 启动类
```

### 1.2 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 包名 | 全小写 | `com.platform.blog` |
| 类名 | 大驼峰 | `ArticleService` |
| 方法名 | 小驼峰 | `getById()` |
| 常量 | 全大写下划线 | `MAX_PAGE_SIZE` |
| 变量 | 小驼峰 | `articleId` |
| 数据库表 | 下划线 | `biz_article` |
| 数据库字段 | 下划线 | `create_time` |

---

## 2. Java 编码规范

### 2.1 类注释

```java
/**
 * 文章服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    // ...
}
```

### 2.2 方法注释

```java
/**
 * 根据ID查询文章
 *
 * @param id 文章ID
 * @return 文章详情
 * @throws BusinessException 文章不存在时抛出
 */
@Override
public ArticleDetailVO getById(Long id) {
    // ...
}
```

### 2.3 Lombok 使用

```java
@Data                          # DTO/VO/Entity
@EqualsAndHashCode(callSuper = true)  # 继承 BaseEntity 的实体
@Slf4j                         # Service 实现类
@Service                       # Service 实现类
@RequiredArgsConstructor       # 构造器注入
@RestController                # Controller
@Mapper(componentModel = "spring")  # MapStruct
```

### 2.4 依赖注入

**推荐**：构造器注入（使用 `@RequiredArgsConstructor`）

```java
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleMapper articleMapper;
    private final ArticleConvert articleConvert;
}
```

**禁止**：字段注入（`@Autowired`）

---

## 3. Spring Boot 规范

### 3.1 启动类

```java
@SpringBootApplication(exclude = {
    OpenAiAutoConfiguration.class  // 排除不需要的自动配置
})
@EnableDiscoveryClient
@MapperScan("com.platform.{service}.mapper")
@ComponentScan(basePackages = {"com.platform.{service}", "com.platform.common"})
public class {Service}Application {
    public static void main(String[] args) {
        SpringApplication.run({Service}Application.class, args);
    }
}
```

### 3.2 配置文件

```yaml
server:
  port: 108X

spring:
  application:
    name: {service-name}
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://172.18.0.34:5432/platform
    username: postgres
    password: postgres

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: ASSIGN_ID
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

---

## 4. MyBatis-Plus 规范

### 4.1 实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_article")
public class Article extends BaseEntity {

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField(exist = false)  # 非数据库字段
    private String categoryName;
}
```

### 4.2 BaseEntity

```java
@Data
public abstract class BaseEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
```

### 4.3 Mapper

```java
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    # 一般不需要自定义方法，MyBatis-Plus 内置方法足够
}
```

### 4.4 查询条件

```java
# LambdaQueryWrapper
LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
wrapper.like(StringUtils.hasText(query.getTitle()), Article::getTitle, query.getTitle());
wrapper.eq(query.getCategoryId() != null, Article::getCategoryId, query.getCategoryId());
wrapper.eq(Article::getStatus, 1);
wrapper.orderByDesc(Article::getCreateTime);

# 分页查询
Page<Article> page = new Page<>(query.getCurrent(), query.getSize());
Page<Article> result = articleMapper.selectPage(page, wrapper);
```

---

## 5. 分层规范

### 5.1 Controller 层

```java
@Tag(name = "文章管理", description = "文章CRUD相关接口")
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "分页查询文章")
    @GetMapping
    public Result<PageResult<ArticleVO>> page(ArticleQueryDTO queryDTO) {
        return Result.success(articleService.page(queryDTO));
    }

    @Operation(summary = "创建文章")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ArticleCreateDTO dto) {
        return Result.success(articleService.create(dto));
    }
}
```

**规范**：
- 使用 `@Tag` + `@Operation` 生成 API 文档
- 使用 `@Valid` 校验参数
- 直接返回 `Result<T>`，不处理业务逻辑

### 5.2 Service 层

**接口**：

```java
public interface ArticleService {
    PageResult<ArticleVO> page(ArticleQueryDTO queryDTO);
    ArticleDetailVO getById(Long id);
    Long create(ArticleCreateDTO dto);
    void update(ArticleUpdateDTO dto);
    void delete(Long id);
}
```

**实现**：

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleConvert articleConvert;

    @Override
    public PageResult<ArticleVO> page(ArticleQueryDTO queryDTO) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        // ...

        Page<Article> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Article> result = articleMapper.selectPage(page, wrapper);

        List<ArticleVO> records = articleConvert.entityListToVOList(result.getRecords());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ArticleCreateDTO dto) {
        Article article = articleConvert.createDTOToEntity(dto);
        // 设置默认值
        article.setStatus(0);
        articleMapper.insert(article);
        return article.getId();
    }
}
```

**规范**：
- 写操作必须加 `@Transactional(rollbackFor = Exception.class)`
- 查询不到数据时抛出 `BusinessException(ResultCode.DATA_NOT_FOUND, "xxx不存在")`
- 使用 MapStruct 进行对象转换

### 5.3 DTO 层

```java
@Data
public class ArticleCreateDTO {

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题不能超过200个字符")
    private String title;

    @Size(max = 500, message = "文章摘要不能超过500个字符")
    private String summary;

    private String content;

    private Long categoryId;

    private List<Long> tagIds;
}
```

### 5.4 VO 层

```java
@Data
public class ArticleVO {
    private Long id;
    private String title;
    private String summary;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
```

### 5.5 Convert 层

```java
@Mapper(componentModel = "spring")
public interface ArticleConvert {

    ArticleVO entityToVO(Article entity);

    List<ArticleVO> entityListToVOList(List<Article> entities);

    Article createDTOToEntity(ArticleCreateDTO dto);

    void updateEntityFromDTO(ArticleUpdateDTO dto, @MappingTarget Article entity);
}
```

---

## 6. 统一返回规范

### 6.1 Result 结构

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 6.2 使用方式

```java
// 成功
return Result.success();
return Result.success(data);
return Result.success("操作成功", data);

// 失败
return Result.fail("参数错误");
return Result.fail(ResultCode.PARAM_INVALID);
return Result.fail(500, "系统异常");
```

### 6.3 分页返回

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "current": 1,
    "size": 10
  }
}
```

```java
return PageResult.of(records, total, current, size);
```

---

## 7. 异常处理规范

### 7.1 业务异常

```java
// 抛出业务异常
throw new BusinessException("文章不存在");
throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文章不存在");
throw new BusinessException(5000, "自定义错误码");
```

### 7.2 ResultCode 枚举

```java
@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "success"),
    FAIL(500, "系统异常"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    BAD_REQUEST(400, "请求参数错误"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(5000, "业务异常"),
    DATA_NOT_FOUND(5002, "数据不存在");

    private final Integer code;
    private final String message;
}
```

### 7.3 全局异常处理

`GlobalExceptionHandler` 已在 `common-web` 中实现，自动处理：
- `BusinessException` → 返回业务错误
- `MethodArgumentNotValidException` → 参数校验失败
- `Exception` → 系统异常

**不需要在 Controller 中 try-catch**。

---

## 8. 日志规范

### 8.1 日志配置

每个服务使用 `logback-spring.xml`，日志输出到 `logs/{service-name}/` 目录。

### 8.2 日志使用

```java
@Slf4j
public class ArticleServiceImpl {

    public void create(ArticleCreateDTO dto) {
        log.info("创建文章, title: {}", dto.getTitle());
        // ...
        log.warn("文章标题过长, title: {}", dto.getTitle());
        // ...
        log.error("创建文章失败", e);
    }
}
```

**规范**：
- 使用 Lombok `@Slf4j` 注解
- 使用 `{}` 占位符，不要字符串拼接
- 异常日志必须带异常对象：`log.error("xxx失败", e)`

---

## 9. Git 提交规范

### 9.1 Commit 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 9.2 Type 类型

| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复 Bug |
| docs | 文档 |
| style | 代码格式（不影响功能） |
| refactor | 重构 |
| perf | 性能优化 |
| test | 测试 |
| chore | 构建/工具 |

### 9.3 示例

```
feat(blog): 添加文章分块向量检索功能

- 实现文章内容自动分块
- 集成 pgvector 向量存储
- 支持语义相似度搜索

Closes #123
```

---

## 10. 常用代码模板

### 10.1 CRUD Service 模板

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class XxxServiceImpl implements XxxService {

    private final XxxMapper xxxMapper;
    private final XxxConvert xxxConvert;

    @Override
    public PageResult<XxxVO> page(XxxQueryDTO queryDTO) {
        LambdaQueryWrapper<Xxx> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getName()), Xxx::getName, queryDTO.getName());
        wrapper.orderByDesc(Xxx::getCreateTime);

        Page<Xxx> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Xxx> result = xxxMapper.selectPage(page, wrapper);

        List<XxxVO> records = xxxConvert.entityListToVOList(result.getRecords());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public XxxVO getById(Long id) {
        Xxx xxx = xxxMapper.selectById(id);
        if (xxx == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "xxx不存在");
        }
        return xxxConvert.entityToVO(xxx);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(XxxCreateDTO dto) {
        Xxx xxx = xxxConvert.createDTOToEntity(dto);
        xxxMapper.insert(xxx);
        return xxx.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(XxxUpdateDTO dto) {
        Xxx xxx = xxxMapper.selectById(dto.getId());
        if (xxx == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "xxx不存在");
        }
        xxxConvert.updateEntityFromDTO(dto, xxx);
        xxxMapper.updateById(xxx);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Xxx xxx = xxxMapper.selectById(id);
        if (xxx == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "xxx不存在");
        }
        xxxMapper.deleteById(id);
    }
}
```

### 10.2 Controller 模板

```java
@Tag(name = "XXX管理", description = "XXX相关接口")
@RestController
@RequestMapping("/xxx")
@RequiredArgsConstructor
public class XxxController {

    private final XxxService xxxService;

    @Operation(summary = "分页查询")
    @GetMapping
    public Result<PageResult<XxxVO>> page(XxxQueryDTO queryDTO) {
        return Result.success(xxxService.page(queryDTO));
    }

    @Operation(summary = "查看详情")
    @GetMapping("/{id}")
    public Result<XxxVO> getById(@PathVariable Long id) {
        return Result.success(xxxService.getById(id));
    }

    @Operation(summary = "创建")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody XxxCreateDTO dto) {
        return Result.success(xxxService.create(dto));
    }

    @Operation(summary = "更新")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody XxxUpdateDTO dto) {
        xxxService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        xxxService.delete(id);
        return Result.success();
    }
}
```

---

## 11. 注意事项

### 11.1 禁止事项

- ❌ 使用 `@Autowired` 字段注入
- ❌ 在 Controller 中处理业务逻辑
- ❌ 使用 `System.out.println()` 输出日志
- ❌ 直接返回 Entity，必须转换为 VO
- ❌ SQL 写在 Java 代码中（使用 MyBatis-Plus）
- ❌ 不处理异常直接抛出

### 11.2 必须事项

- ✅ 使用构造器注入
- ✅ 写操作加 `@Transactional`
- ✅ 使用 MapStruct 转换对象
- ✅ 查询空数据抛 `BusinessException`
- ✅ 使用 `@Valid` 校验参数
- ✅ 使用 `log.xxx()` 记录日志
