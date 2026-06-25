# Agent 服务优化方案

## 一、当前问题分析

### 1.1 架构问题

| 问题 | 影响 | 严重程度 |
|------|------|----------|
| 同步执行 | 任务阻塞，吞吐量低 | 高 |
| 无重试机制 | 失败任务无法恢复 | 高 |
| 无超时控制 | 任务可能无限挂起 | 高 |
| 无执行历史 | 无法追溯问题 | 中 |
| 无并发控制 | 资源竞争，系统不稳定 | 中 |
| 无熔断降级 | 外部服务故障影响整体 | 中 |

### 1.2 代码问题

```java
// 当前问题：同步执行，无超时，无重试
@Override
public void executeTask(Long taskId, String token) {
    // 整个流程是同步的，任何一步失败都会导致任务阻塞
    // 没有超时控制，可能无限等待
    // 没有重试机制，失败就直接标记为 BLOCKED
}
```

---

## 二、优化方案

### 2.1 异步任务执行

**目标**：将同步执行改为异步，提高吞吐量

```java
// 优化后：异步执行 + 超时控制
@Async("agentTaskExecutor")
public CompletableFuture<TaskExecutionResult> executeTaskAsync(Long taskId, String token) {
    return CompletableFuture.supplyAsync(() -> {
        return executeWithTimeout(taskId, token, Duration.ofMinutes(30));
    }, taskExecutor);
}
```

### 2.2 任务队列管理

**目标**：使用线程池管理并发任务

```java
@Configuration
public class AgentThreadPoolConfig {
    
    @Bean("agentTaskExecutor")
    public ThreadPoolTaskExecutor agentTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);          // 核心线程数
        executor.setMaxPoolSize(8);           // 最大线程数
        executor.setQueueCapacity(100);       // 队列容量
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("agent-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
```

### 2.3 超时控制

**目标**：防止单个任务长时间占用资源

```java
@Service
public class TaskTimeoutService {
    
    private final Map<Long, Future<?>> runningTasks = new ConcurrentHashMap<>();
    
    public TaskExecutionResult executeWithTimeout(Long taskId, Supplier<TaskExecutionResult> task, Duration timeout) {
        Future<?> future = taskExecutor.submit(() -> {
            try {
                runningTasks.put(taskId, ...);
                task.get();
            } finally {
                runningTasks.remove(taskId);
            }
        });
        
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new TaskTimeoutException("任务执行超时: " + taskId);
        }
    }
    
    public void cancelTask(Long taskId) {
        Future<?> future = runningTasks.get(taskId);
        if (future != null) {
            future.cancel(true);
        }
    }
}
```

### 2.4 重试机制

**目标**：自动重试失败的任务

```java
@Service
public class TaskRetryService {
    
    @Retryable(
        value = {TransientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    public TaskExecutionResult executeWithRetry(Long taskId) {
        return agentService.executeTask(taskId);
    }
    
    @Recover
    public TaskExecutionResult recover(Exception e, Long taskId) {
        log.error("任务重试失败，标记为 BLOCKED: {}", taskId, e);
        projectServiceClient.blockTask(taskId, "重试失败: " + e.getMessage());
        return TaskExecutionResult.failed(e.getMessage());
    }
}
```

### 2.5 熔断降级

**目标**：外部服务故障时快速失败

```java
@Service
public class ResilientServiceCaller {
    
    @CircuitBreaker(name = "projectService", fallbackMethod = "fallback")
    @Retry(name = "projectService")
    @TimeLimiter(name = "projectService")
    public CompletableFuture<Result> callProjectService(Supplier<Result> call) {
        return CompletableFuture.supplyAsync(call);
    }
    
    public CompletableFuture<Result> fallback(Supplier<Result> call, Throwable t) {
        log.warn("熔断降级: {}", t.getMessage());
        return CompletableFuture.completedFuture(Result.fail("服务暂时不可用"));
    }
}
```

### 2.6 执行历史持久化

**目标**：记录每次执行的详细信息

```java
@Entity
@Table(name = "agent_execution")
public class AgentExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long taskId;
    private Long projectId;
    private Integer status;      // 0:待执行 1:执行中 2:成功 3:失败 4:超时 5:取消
    private String executorId;   // 执行器实例 ID
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String errorMessage;
    private String resultSummary;
    
    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL)
    private List<ExecutionStep> steps;
}

@Entity
@Table(name = "agent_execution_step")
public class ExecutionStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long executionId;
    private String stepName;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String detail;
    private String errorMessage;
}
```

### 2.7 健康检查与监控

**目标**：实时监控 Agent 服务状态

```java
@Component
public class AgentHealthIndicator implements HealthIndicator {
    
    private final ThreadPoolTaskExecutor agentTaskExecutor;
    private final TaskTimeoutService taskTimeoutService;
    
    @Override
    public Health health() {
        int activeCount = agentTaskExecutor.getActiveCount();
        int queueSize = agentTaskExecutor.getQueueSize();
        int maxPoolSize = agentTaskExecutor.getMaxPoolSize();
        
        if (activeCount >= maxPoolSize * 0.9) {
            return Health.down()
                .withDetail("reason", "线程池接近满载")
                .withDetail("activeCount", activeCount)
                .withDetail("queueSize", queueSize)
                .build();
        }
        
        return Health.up()
            .withDetail("activeCount", activeCount)
            .withDetail("queueSize", queueSize)
            .withDetail("maxPoolSize", maxPoolSize)
            .build();
    }
}
```

---

## 三、实施计划

### 阶段一：基础优化（1 周）

| 任务 | 工作量 | 说明 |
|------|--------|------|
| 异步执行改造 | 2d | 线程池 + CompletableFuture |
| 超时控制 | 1d | 防止任务无限挂起 |
| 执行历史表 | 1d | 持久化执行记录 |
| 前端执行记录页面 | 1d | 查看执行历史 |

### 阶段二：可靠性提升（1 周）

| 任务 | 工作量 | 说明 |
|------|--------|------|
| 重试机制 | 1d | 自动重试失败任务 |
| 熔断降级 | 1d | 外部服务故障保护 |
| 健康检查 | 1d | 服务状态监控 |
| 任务取消功能 | 1d | 支持手动取消任务 |

### 阶段三：高级特性（1 周）

| 任务 | 工作量 | 说明 |
|------|--------|------|
| 任务依赖管理 | 2d | 支持任务依赖关系 |
| 并发控制 | 1d | 限制同时执行的任务数 |
| 优先级队列 | 1d | 高优先级任务优先执行 |
| 执行统计报表 | 1d | 执行成功率、耗时统计 |

---

## 四、核心代码示例

### 4.1 重构后的 AgentService

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final ThreadPoolTaskExecutor agentTaskExecutor;
    private final TaskTimeoutService taskTimeoutService;
    private final TaskRetryService taskRetryService;
    private final ExecutionHistoryService executionHistoryService;
    private final WorkspaceService workspaceService;
    private final CodeGenerationService codeGenerationService;
    private final McpToolService mcpToolService;
    private final ProjectServiceClient projectServiceClient;

    @Override
    public CompletableFuture<TaskExecutionResult> executeTaskAsync(Long taskId) {
        return executeTaskAsync(taskId, null);
    }

    @Override
    public CompletableFuture<TaskExecutionResult> executeTaskAsync(Long taskId, String token) {
        // 创建执行记录
        Long executionId = executionHistoryService.createExecution(taskId);
        
        return CompletableFuture.supplyAsync(() -> {
            return taskTimeoutService.executeWithTimeout(taskId, () -> {
                return doExecuteTask(taskId, token, executionId);
            }, Duration.ofMinutes(30));
        }, agentTaskExecutor).exceptionally(e -> {
            log.error("任务执行异常: {}", taskId, e);
            executionHistoryService.failExecution(executionId, e.getMessage());
            return TaskExecutionResult.failed(e.getMessage());
        });
    }

    private TaskExecutionResult doExecuteTask(Long taskId, String token, Long executionId) {
        String workspacePath = null;
        
        try {
            // 1. 更新状态为执行中
            executionHistoryService.startExecution(executionId);
            projectServiceClient.startTask(taskId);
            
            // 2. 创建工作区（带步骤记录）
            executionHistoryService.addStep(executionId, "创建工作区", "执行中");
            workspacePath = workspaceService.createWorkspace(taskId);
            executionHistoryService.addStep(executionId, "创建工作区", "成功");
            
            // 3. 知识库检索
            executionHistoryService.addStep(executionId, "知识库检索", "执行中");
            String context = mcpToolService.knowledgeSearch("...");
            executionHistoryService.addStep(executionId, "知识库检索", "成功");
            
            // 4. 代码生成
            executionHistoryService.addStep(executionId, "代码生成", "执行中");
            Map<String, String> codeFiles = codeGenerationService.generateCode(context, getTaskDescription(taskId));
            executionHistoryService.addStep(executionId, "代码生成", "成功");
            
            // 5. 写入文件
            executionHistoryService.addStep(executionId, "写入文件", "执行中");
            for (Map.Entry<String, String> entry : codeFiles.entrySet()) {
                workspaceService.writeFile(workspacePath, entry.getKey(), entry.getValue());
            }
            executionHistoryService.addStep(executionId, "写入文件", "成功");
            
            // 6. 编译
            executionHistoryService.addStep(executionId, "编译", "执行中");
            boolean compileResult = workspaceService.compile(workspacePath);
            if (!compileResult) {
                throw new RuntimeException("编译失败");
            }
            executionHistoryService.addStep(executionId, "编译", "成功");
            
            // 7. Git 提交
            executionHistoryService.addStep(executionId, "Git 提交", "执行中");
            workspaceService.commit(workspacePath, "feat: AI auto-generated code for task " + taskId);
            executionHistoryService.addStep(executionId, "Git 提交", "成功");
            
            // 8. 更新任务状态为完成
            projectServiceClient.completeTask(taskId);
            executionHistoryService.completeExecution(executionId, "执行成功");
            
            return TaskExecutionResult.success("执行成功");
            
        } catch (Exception e) {
            log.error("任务执行失败: {}", taskId, e);
            
            // 更新任务状态为阻塞
            try {
                projectServiceClient.blockTask(taskId, "Agent 执行失败: " + e.getMessage());
            } catch (Exception ex) {
                log.error("更新任务状态失败", ex);
            }
            
            executionHistoryService.failExecution(executionId, e.getMessage());
            return TaskExecutionResult.failed(e.getMessage());
            
        } finally {
            // 清理工作区（可配置）
            if (workspacePath != null && agentConfig.isAutoCleanup()) {
                workspaceService.cleanup(taskId);
            }
        }
    }
}
```

### 4.2 任务取消功能

```java
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final TaskTimeoutService taskTimeoutService;

    @Operation(summary = "取消正在执行的任务")
    @PostMapping("/tasks/{taskId}/cancel")
    public Result<Void> cancelTask(@PathVariable Long taskId) {
        boolean cancelled = taskTimeoutService.cancelTask(taskId);
        if (cancelled) {
            return Result.success();
        } else {
            return Result.fail("任务不存在或已完成");
        }
    }
}
```

---

## 五、配置示例

```yaml
agent:
  # 线程池配置
  thread-pool:
    core-size: 4
    max-size: 8
    queue-capacity: 100
    keep-alive: 60
    
  # 超时配置
  timeout:
    default: 30m
    max: 2h
    
  # 重试配置
  retry:
    max-attempts: 3
    delay: 5s
    multiplier: 2
    
  # 熔断配置
  circuit-breaker:
    failure-rate-threshold: 50
    wait-duration: 30s
    
  # 工作区配置
  workspace:
    base-path: /tmp/agent-workspace
    auto-cleanup: true
    git-url: https://github.com/xxx/xxx.git
```

---

## 六、预期效果

| 指标 | 优化前 | 优化后 |
|------|--------|--------|
| 任务吞吐量 | 1 个/30min | 4-8 个/30min |
| 失败恢复 | 手动 | 自动重试 |
| 超时处理 | 无 | 自动取消 |
| 执行追溯 | 无 | 完整记录 |
| 故障隔离 | 无 | 熔断降级 |
| 监控能力 | 无 | 健康检查 |
