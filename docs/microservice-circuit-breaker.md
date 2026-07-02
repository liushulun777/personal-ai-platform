# 微服务熔断与降级详解

> 以 Personal AI Platform 系统为实例，深入理解微服务熔断与降级的原理、实践和最佳实践。
> 本文档用于知识学习和 AI 代码生成参考。

---

## 〇、微服务核心概念速览

| 概念 | 解决的问题 | 举例 |
|------|------------|------|
| **服务注册** | 服务在哪里 | Nacos 注册服务 |
| **服务发现** | 找到服务实例 | 根据 `user-service` 获取 IP |
| **负载均衡** | 调哪一个实例 | 默认轮询 |
| **超时** | 避免一直等待 | 5 秒没响应直接返回 |
| **重试** | 临时失败自动重试 | 网络抖动再请求一次 |
| **熔断** | 连续失败停止调用 | 保护自己和下游服务 |
| **降级** | 返回兜底结果 | "评论暂不可用" |
| **限流** | 控制请求数量 | 每秒最多 1000 次 |
| **隔离** | 避免互相影响 | 登录和订单使用不同线程池 |
| **配置中心** | 配置统一管理 | Nacos 配置数据库地址 |

> **注**：本文档重点讲解**熔断**与**降级**，其他概念可参考 [微服务核心概念与实践指南](./.claude/docs/microservice-knowledge.md)。

---

## 一、熔断概念与原理

### 1.1 什么是熔断？
熔断（Circuit Breaker）是一种容错机制，当某个服务或资源出现故障时，暂时停止对它的调用，避免故障扩散，保证系统整体可用性。

### 1.2 为什么需要熔断？
在微服务架构中，服务间调用链复杂。如果某个服务出现故障（如响应超时、异常率过高），调用方可能会：
- **资源耗尽**：大量线程阻塞等待响应
- **级联故障**：故障扩散到上游服务
- **系统雪崩**：整个系统不可用

### 1.3 熔断状态机
熔断器有三种状态：

```
关闭（Closed）→ 打开（Open）→ 半开（Half-Open）→ 关闭（Closed）
```

| 状态 | 行为 | 转换条件 |
|------|------|----------|
| **关闭** | 正常调用，统计失败率 | 失败率超过阈值 → 打开 |
| **打开** | 快速失败，不调用下游 | 等待超时 → 半开 |
| **半开** | 允许少量请求试探 | 成功 → 关闭；失败 → 打开 |

### 1.4 关键指标
- **失败率（Failure Rate）**：失败请求 / 总请求
- **慢调用率（Slow Call Rate）**：响应时间超过阈值的请求比例
- **阈值（Threshold）**：触发熔断的临界值
- **等待时长（Wait Duration）**：熔断器打开后，进入半开状态的等待时间

---

## 二、常见实现方案对比

| 特性 | Sentinel | Resilience4j | Hystrix |
|------|----------|--------------|---------|
| **维护状态** | 活跃 | 活跃 | 停止维护 |
| **Spring Cloud 集成** | Spring Cloud Alibaba 原生 | 手动配置 | Spring Cloud Netflix |
| **配置方式** | 注解 + 规则动态推送 | 注解 + 配置文件 | 注解 + 配置文件 |
| **实时监控** | Dashboard 控制台 | Micrometer 指标 | Hystrix Dashboard |
| **熔断策略** | 慢调用比例、异常比例、异常数 | 慢调用比例、异常比例、异常数 | 异常比例 |
| **限流** | 支持（QPS、线程数） | 不支持 | 不支持 |
| **热点参数限流** | 支持 | 不支持 | 不支持 |
| **集群流控** | 支持 | 不支持 | 不支持 |
| **动态规则** | 支持（Nacos、Zookeeper） | 不支持 | 不支持 |
| **学习曲线** | 中等 | 简单 | 简单 |

### 选型建议
- **Spring Cloud Alibaba 项目** → 优先选择 Sentinel（生态契合、功能全面）
- **轻量级需求** → Resilience4j（简单、无额外依赖）
- **已使用 Hystrix** → 建议迁移到 Sentinel 或 Resilience4j

---

## 三、Spring Cloud Alibaba Sentinel 集成指南

### 3.1 添加依赖

```xml
<!-- 在需要熔断的服务中添加 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>

<!-- 如需使用 Sentinel Dashboard（可选） -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-sentinel-dashboard</artifactId>
</dependency>
```

### 3.2 配置文件

```yaml
# application.yml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080  # Sentinel Dashboard 地址
        port: 8719                 # 与 Dashboard 通信端口
      # 懒加载（首次调用时初始化）
      eager: false
      
# 如需使用 Nacos 动态规则推送
sentinel:
  datasource:
    flow:
      nacos:
        server-addr: ${spring.cloud.nacos.server-addr}
        dataId: ${spring.application.name}-flow-rules
        groupId: SENTINEL_GROUP
        rule-type: flow
    degrade:
      nacos:
        server-addr: ${spring.cloud.nacos.server-addr}
        dataId: ${spring.application.name}-degrade-rules
        groupId: SENTINEL_GROUP
        rule-type: degrade
```

### 3.3 注解方式使用

```java
@Service
public class UserServiceClient {
    
    /**
     * 调用用户服务获取用户信息
     * @SentinelResource 参数说明：
     * - value: 资源名称（必填，用于规则匹配）
     * - blockHandler: 限流/降级处理方法（参数需包含 BlockException）
     * - fallback: 业务异常处理方法（参数需包含 Throwable）
     * - exceptionsToIgnore: 忽略的异常类型
     */
    @SentinelResource(
        value = "getUserById",
        blockHandler = "getUserByIdBlockHandler",
        fallback = "getUserByIdFallback",
        exceptionsToIgnore = {IllegalArgumentException.class}
    )
    public UserDTO getUserById(Long id) {
        // 调用远程服务
        return userFeignClient.getUserById(id).getData();
    }
    
    /**
     * 限流/降级处理方法（BlockException）
     */
    public UserDTO getUserByIdBlockHandler(Long id, BlockException ex) {
        log.warn("用户服务被限流或降级: {}", ex.getClass().getSimpleName());
        return UserDTO.builder()
            .id(id)
            .nickname("用户信息获取失败")
            .build();
    }
    
    /**
     * 业务异常处理方法（Throwable）
     */
    public UserDTO getUserByIdFallback(Long id, Throwable t) {
        log.error("用户服务调用失败: {}", t.getMessage());
        return UserDTO.builder()
            .id(id)
            .nickname("用户信息获取失败")
            .build();
    }
}
```

### 3.4 编程方式使用

```java
@Service
public class ResilientServiceCaller {
    
    /**
     * 编程方式使用 Sentinel
     */
    public UserDTO getUserById(Long id) {
        String resource = "getUserById";
        
        Entry entry = null;
        try {
            // 1. 获取资源入口
            entry = SphU.entry(resource);
            
            // 2. 执行业务逻辑
            return userFeignClient.getUserById(id).getData();
            
        } catch (BlockException ex) {
            // 3. 限流/降级处理
            log.warn("用户服务被限流或降级: {}", ex.getClass().getSimpleName());
            return getDefaultUser(id);
            
        } catch (Exception ex) {
            // 4. 业务异常统计（触发降级规则）
            Tracer.trace(ex);
            throw ex;
            
        } finally {
            // 5. 释放资源
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
```

### 3.5 Feign 集成 Sentinel

```java
// 1. 开启 Feign 对 Sentinel 的支持
@FeignClient(
    name = "system-service",
    fallbackFactory = UserServiceClientFallbackFactory.class
)
public interface UserServiceClient {
    @GetMapping("/users/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);
}

// 2. 实现 FallbackFactory
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {
    
    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public Result<UserDTO> getUserById(Long id) {
                log.error("用户服务调用失败，降级处理: {}", cause.getMessage());
                UserDTO defaultUser = UserDTO.builder()
                    .id(id)
                    .nickname("用户信息获取失败")
                    .build();
                return Result.success(defaultUser);
            }
        };
    }
}
```

```yaml
# 开启 Feign 对 Sentinel 的支持
feign:
  sentinel:
    enabled: true
```

---

## 四、Resilience4j 集成指南

### 4.1 添加依赖

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>

<!-- 如需 AOP 支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### 4.2 配置文件

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      userService:
        # 滑动窗口类型：COUNT_BASED（基于计数）或 TIME_BASED（基于时间）
        sliding-window-type: COUNT_BASED
        # 滑动窗口大小
        sliding-window-size: 10
        # 最小调用次数（低于此次数不计算失败率）
        minimum-number-of-calls: 5
        # 失败率阈值（百分比）
        failure-rate-threshold: 50
        # 慢调用率阈值（百分比）
        slow-call-rate-threshold: 100
        # 慢调用持续时间阈值
        slow-call-duration-threshold: 2s
        # 等待时长（从打开到半开）
        wait-duration-in-open-state: 30s
        # 半开状态允许的调用次数
        permitted-number-of-calls-in-half-open-state: 3
        # 是否自动从打开过渡到半开
        automatic-transition-from-open-to-half-open-enabled: true
        # 记录为失败的异常
        record-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
          - org.springframework.web.client.HttpServerErrorException
        # 忽略的异常
        ignore-exceptions:
          - java.lang.IllegalArgumentException
          
  # 重试配置
  retry:
    instances:
      userService:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2
        retry-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
        ignore-exceptions:
          - java.lang.IllegalArgumentException
          
  # 超时配置
  timelimiter:
    instances:
      userService:
        timeout-duration: 5s
        cancel-running-future: true
```

### 4.3 注解方式使用

```java
@Service
public class UserServiceClient {
    
    /**
     * @CircuitBreaker 参数说明：
     * - name: 断路器实例名称（对应配置文件中的 instances）
     * - fallbackMethod: 降级方法名
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "userService")
    @TimeLimiter(name = "userService")
    public CompletableFuture<UserDTO> getUserById(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            return userFeignClient.getUserById(id).getData();
        });
    }
    
    /**
     * 降级方法（参数需包含原始参数 + Throwable）
     */
    public CompletableFuture<UserDTO> getUserByIdFallback(Long id, Throwable t) {
        log.error("用户服务调用失败，降级处理: {}", t.getMessage());
        UserDTO defaultUser = UserDTO.builder()
            .id(id)
            .nickname("用户信息获取失败")
            .build();
        return CompletableFuture.completedFuture(defaultUser);
    }
}
```

### 4.4 编程方式使用

```java
@Service
public class ResilientServiceCaller {
    
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    
    public UserDTO getUserById(Long id) {
        // 1. 获取断路器实例
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("userService");
        
        // 2. 获取重试实例
        Retry retry = retryRegistry.retry("userService");
        
        // 3. 编排调用
        Supplier<UserDTO> supplier = () -> userFeignClient.getUserById(id).getData();
        
        // 4. 装饰调用
        Supplier<UserDTO> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        decoratedSupplier = Retry.decorateSupplier(retry, decoratedSupplier);
        
        // 5. 执行调用
        try {
            return decoratedSupplier.get();
        } catch (CallNotPermittedException ex) {
            // 断路器打开
            log.warn("断路器打开，快速失败: {}", ex.getMessage());
            return getDefaultUser(id);
        } catch (Exception ex) {
            // 其他异常
            log.error("用户服务调用失败: {}", ex.getMessage());
            return getDefaultUser(id);
        }
    }
}
```

---

## 五、熔断配置参数详解

### 5.1 Sentinel 关键参数

| 参数 | 说明 | 默认值 | 建议值 |
|------|------|--------|--------|
| `grade` | 熔断策略（0=慢调用比例，1=异常比例，2=异常数） | 0 | 根据业务选择 |
| `count` | 阈值（慢调用比例/异常比例/异常数） | - | 慢调用比例：0.5；异常比例：0.5 |
| `timeWindow` | 熔断时长（秒） | 10 | 30-60 |
| `slowRatioThreshold` | 慢调用比例阈值 | 1.0 | 0.5-0.8 |
| `minRequestAmount` | 最小请求数（低于此数不计算） | 5 | 10-20 |
| `statIntervalMs` | 统计时长（毫秒） | 1000 | 1000-10000 |

### 5.2 Resilience4j 关键参数

| 参数 | 说明 | 默认值 | 建议值 |
|------|------|--------|--------|
| `sliding-window-size` | 滑动窗口大小 | 100 | 10-100 |
| `minimum-number-of-calls` | 最小调用次数 | 100 | 5-20 |
| `failure-rate-threshold` | 失败率阈值（%） | 50 | 50-60 |
| `slow-call-rate-threshold` | 慢调用率阈值（%） | 100 | 80-100 |
| `slow-call-duration-threshold` | 慢调用持续时间 | 60s | 1-5s |
| `wait-duration-in-open-state` | 打开状态等待时长 | 60s | 30-60s |
| `permitted-number-of-calls-in-half-open-state` | 半开状态允许调用数 | 10 | 3-10 |

---

## 六、项目应用场景

### 6.1 Feign 调用熔断（推荐）

**场景**：blog-service 调用 system-service 获取用户信息

```java
// 1. Feign 客户端配置
@FeignClient(
    name = "system-service",
    fallbackFactory = UserServiceClientFallbackFactory.class
)
public interface UserServiceClient {
    @GetMapping("/users/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);
    
    @PostMapping("/users/batch")
    Result<List<UserDTO>> getUserByIds(@RequestBody List<Long> ids);
}

// 2. FallbackFactory 实现
@Component
@Slf4j
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {
    
    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public Result<UserDTO> getUserById(Long id) {
                log.error("用户服务调用失败: {}", cause.getMessage());
                return Result.success(UserDTO.builder()
                    .id(id)
                    .nickname("用户信息获取失败")
                    .build());
            }
            
            @Override
            public Result<List<UserDTO>> getUserByIds(List<Long> ids) {
                log.error("用户服务批量调用失败: {}", cause.getMessage());
                List<UserDTO> defaultUsers = ids.stream()
                    .map(id -> UserDTO.builder()
                        .id(id)
                        .nickname("用户信息获取失败")
                        .build())
                    .collect(Collectors.toList());
                return Result.success(defaultUsers);
            }
        };
    }
}

// 3. 业务使用
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    
    private final UserServiceClient userServiceClient;
    
    @Override
    public ArticleDetailVO getArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        
        // 调用用户服务（已配置熔断降级）
        UserDTO user = userServiceClient.getUserById(article.getAuthorId()).getData();
        
        ArticleDetailVO vo = new ArticleDetailVO();
        BeanUtils.copyProperties(article, vo);
        vo.setAuthorName(user.getNickname());
        vo.setAuthorAvatar(user.getAvatar());
        
        return vo;
    }
}
```

### 6.2 外部 API 调用熔断

**场景**：AI 服务调用外部大模型 API

```java
@Service
@Slf4j
public class AiModelService {
    
    @SentinelResource(
        value = "callAiModel",
        blockHandler = "callAiModelBlockHandler",
        fallback = "callAiModelFallback"
    )
    public String callAiModel(String prompt) {
        // 调用外部 AI API
        return aiApiClient.chat(prompt);
    }
    
    /**
     * 限流/降级处理
     */
    public String callAiModelBlockHandler(String prompt, BlockException ex) {
        log.warn("AI 服务被限流或降级: {}", ex.getClass().getSimpleName());
        return "AI 服务暂时不可用，请稍后重试";
    }
    
    /**
     * 业务异常处理
     */
    public String callAiModelFallback(String prompt, Throwable t) {
        log.error("AI 服务调用失败: {}", t.getMessage());
        return "AI 服务调用失败，请稍后重试";
    }
}
```

### 6.3 数据库访问熔断

**场景**：保护数据库连接池

```java
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    
    @SentinelResource(
        value = "queryUser",
        blockHandler = "queryUserBlockHandler",
        fallback = "queryUserFallback"
    )
    @Override
    public UserDTO getUserById(Long id) {
        return userMapper.selectById(id);
    }
    
    public UserDTO queryUserBlockHandler(Long id, BlockException ex) {
        log.warn("用户查询被限流: {}", ex.getClass().getSimpleName());
        return UserDTO.builder()
            .id(id)
            .nickname("查询过于频繁")
            .build();
    }
    
    public UserDTO queryUserFallback(Long id, Throwable t) {
        log.error("用户查询失败: {}", t.getMessage());
        return UserDTO.builder()
            .id(id)
            .nickname("查询失败")
            .build();
    }
}
```

---

## 七、监控与告警

### 7.1 Sentinel Dashboard

```bash
# 启动 Sentinel Dashboard
java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -jar sentinel-dashboard.jar

# 应用配置
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080
```

**Dashboard 功能**：
- 实时监控：QPS、响应时间、异常数
- 簛路器状态：打开/关闭/半开
- 规则管理：动态配置流控、降级、热点规则
- 集群监控：多实例聚合监控

### 7.2 Micrometer 指标集成（Resilience4j）

```yaml
# 开启 Micrometer 指标
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
```

```java
// 自定义指标收集
@Component
public class CircuitBreakerMetrics {
    
    private final MeterRegistry meterRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    public CircuitBreakerMetrics(MeterRegistry meterRegistry, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.meterRegistry = meterRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        
        // 注册指标
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(this::registerMetrics);
    }
    
    private void registerMetrics(CircuitBreaker circuitBreaker) {
        // 失败率
        Gauge.builder("resilience4j.circuitbreaker.failure.rate", 
                circuitBreaker, cb -> cb.getMetrics().getFailureRate())
            .tag("name", circuitBreaker.getName())
            .register(meterRegistry);
        
        // 慢调用率
        Gauge.builder("resilience4j.circuitbreaker.slow.call.rate", 
                circuitBreaker, cb -> cb.getMetrics().getSlowCallRate())
            .tag("name", circuitBreaker.getName())
            .register(meterRegistry);
        
        // 状态
        Gauge.builder("resilience4j.circuitbreaker.state", 
                circuitBreaker, cb -> cb.getState() == CircuitBreaker.State.OPEN ? 1 : 0)
            .tag("name", circuitBreaker.getName())
            .register(meterRegistry);
    }
}
```

### 7.3 告警配置

```yaml
# Prometheus 告警规则示例
groups:
  - name: circuit-breaker-alerts
    rules:
      - alert: CircuitBreakerOpen
        expr: resilience4j_circuitbreaker_state{name="userService"} == 1
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "断路器打开: {{ $labels.name }}"
          description: "服务 {{ $labels.name }} 断路器已打开超过1分钟"
          
      - alert: HighFailureRate
        expr: resilience4j_circuitbreaker_failure_rate{name="userService"} > 50
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "高失败率: {{ $labels.name }}"
          description: "服务 {{ $labels.name }} 失败率超过50%持续5分钟"
```

---

## 八、最佳实践

### 8.1 熔断策略选择

| 场景 | 推荐策略 | 说明 |
|------|----------|------|
| 响应时间敏感 | 慢调用比例 | 超时即视为失败 |
| 异常敏感 | 异常比例 | 只关注异常率 |
| 流量突增 | 异常数 | 固定阈值，避免误判 |
| 混合场景 | 组合使用 | 同时配置多种策略 |

### 8.2 阈值设置原则

```yaml
# 生产环境建议
failure-rate-threshold: 50      # 失败率 50%
slow-call-rate-threshold: 80    # 慢调用率 80%
slow-call-duration-threshold: 2s # 慢调用定义：2秒
minimum-number-of-calls: 10     # 最小样本数
wait-duration-in-open-state: 30s # 熔断时长
```

### 8.3 降级设计原则

1. **快速失败**：降级响应时间应远小于正常调用
2. **友好提示**：返回用户可理解的错误信息
3. **数据兜底**：返回缓存数据或默认值
4. **功能降级**：关闭非核心功能，保证核心流程

### 8.4 监控指标

| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| 熔断器状态 | 是否打开 | 打开 > 1分钟 |
| 失败率 | 请求失败比例 | > 50% |
| 慢调用率 | 响应慢的比例 | > 80% |
| 降级次数 | 触发降级的次数 | 突增 > 200% |

### 8.5 常见错误

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| 降级方法不生效 | 参数不匹配 | 降级方法参数需包含原始参数 + Throwable |
| 熔断器不打开 | 样本数不足 | 调整 `minimum-number-of-calls` |
| 误判熔断 | 阈值设置不合理 | 根据实际 QPS 调整阈值 |
| 性能下降 | 统计窗口过大 | 缩小 `sliding-window-size` |

---

## 九、常见问题与解决方案

### 9.1 熔断器一直打开怎么办？

**可能原因**：
1. 下游服务确实故障
2. 阈值设置过低
3. 统计窗口过小

**解决方案**：
```yaml
# 调整阈值和窗口
resilience4j:
  circuitbreaker:
    instances:
      userService:
        failure-rate-threshold: 60  # 提高阈值
        sliding-window-size: 20     # 增大窗口
        minimum-number-of-calls: 10 # 增加最小调用数
```

### 9.2 降级方法返回什么？

**原则**：根据业务场景选择

```java
// 1. 返回默认值
public UserDTO fallback(Long id, Throwable t) {
    return UserDTO.builder().id(id).nickname("未知用户").build();
}

// 2. 返回缓存数据
public UserDTO fallback(Long id, Throwable t) {
    return redisTemplate.opsForValue().get("user:" + id);
}

// 3. 抛出业务异常
public UserDTO fallback(Long id, Throwable t) {
    throw new BusinessException("用户服务暂时不可用");
}

// 4. 返回空集合（批量查询）
public List<UserDTO> batchFallback(List<Long> ids, Throwable t) {
    return Collections.emptyList();
}
```

### 9.3 如何测试熔断？

```java
@SpringBootTest
class CircuitBreakerTest {
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Test
    void testCircuitBreaker() {
        // 1. 模拟下游服务故障
        when(userFeignClient.getUserById(any()))
            .thenThrow(new RuntimeException("服务不可用"));
        
        // 2. 连续调用触发熔断
        for (int i = 0; i < 10; i++) {
            UserDTO user = userServiceClient.getUserById(1L);
            assertNotNull(user);
        }
        
        // 3. 验证降级逻辑
        verify(userFeignClient, times(10)).getUserById(any());
    }
}
```

### 9.4 熔断与重试如何配合？

```java
// 推荐顺序：重试 → 熔断 → 超时
@Retry(name = "userService")           // 1. 先重试
@CircuitBreaker(name = "userService")  // 2. 再熔断
@TimeLimiter(name = "userService")     // 3. 最后超时
public CompletableFuture<UserDTO> getUserById(Long id) {
    return CompletableFuture.supplyAsync(() -> {
        return userFeignClient.getUserById(id).getData();
    });
}
```

---

## 十、本系统集成建议

### 10.1 需要熔断的场景

| 服务 | 调用目标 | 熔断策略 | 优先级 |
|------|----------|----------|--------|
| blog-service | system-service（用户信息） | 慢调用比例 | 高 |
| search-service | Elasticsearch | 异常比例 | 高 |
| ai-service | 外部 AI API | 慢调用比例 | 高 |
| project-service | agent-service | 异常比例 | 中 |
| agent-service | project-service | 异常比例 | 中 |

### 10.2 实施步骤

1. **添加依赖**：在 `common-web` 或各服务中添加 Sentinel 依赖
2. **配置规则**：在 `application.yml` 中配置熔断规则
3. **添加注解**：在 Feign 客户端和关键服务方法上添加 `@SentinelResource`
4. **实现降级**：为每个熔断点实现降级逻辑
5. **监控告警**：部署 Sentinel Dashboard，配置 Prometheus 告警
6. **压力测试**：模拟故障场景，验证熔断效果

### 10.3 代码模板

```java
// 1. Feign 客户端模板
@FeignClient(
    name = "target-service",
    fallbackFactory = TargetServiceFallbackFactory.class
)
public interface TargetServiceClient {
    @GetMapping("/api/{id}")
    Result<TargetDTO> getById(@PathVariable Long id);
}

// 2. FallbackFactory 模板
@Component
@Slf4j
public class TargetServiceFallbackFactory implements FallbackFactory<TargetServiceClient> {
    @Override
    public TargetServiceClient create(Throwable cause) {
        return new TargetServiceClient() {
            @Override
            public Result<TargetDTO> getById(Long id) {
                log.error("服务调用失败: {}", cause.getMessage());
                return Result.success(TargetDTO.builder().id(id).name("降级数据").build());
            }
        };
    }
}

// 3. 配置模板
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080
feign:
  sentinel:
    enabled: true
```

---

## 附录：关键配置文件

| 文件 | 说明 |
|------|------|
| `pom.xml` | 添加 Sentinel/Resilience4j 依赖 |
| `application.yml` | 配置熔断规则、Dashboard 地址 |
| `*FallbackFactory.java` | Feign 降级工厂实现 |
| `*BlockHandler.java` | Sentinel 限流/降级处理方法 |

---

## 参考资源

- [Sentinel 官方文档](https://sentinelguard.io/zh-cn/docs/introduction.html)
- [Resilience4j 官方文档](https://resilience4j.readme.io/docs)
- [Spring Cloud Alibaba Sentinel](https://sca.aliyun.com/zh-cn/docs/next/user-guide/sentinel/)
- [Hystrix 已停止维护](https://github.com/Netflix/Hystrix)
