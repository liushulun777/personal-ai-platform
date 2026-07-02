package com.platform.common.web.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 配置类
 */
@Slf4j
@Configuration
public class SentinelConfig {

    /**
     * 注册 Sentinel 资源切面
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * 初始化 Sentinel 规则
     */
    @PostConstruct
    public void initRules() {
        log.info("初始化 Sentinel 规则...");
        initFlowRules();
        initDegradeRules();
    }

    /**
     * 初始化限流规则
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 用户服务限流规则
        FlowRule userRule = new FlowRule();
        userRule.setResource("getUserDTOById");
        userRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userRule.setCount(100); // QPS 限制为 100
        userRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
        userRule.setWarmUpPeriodSec(10); // 预热时长 10 秒
        rules.add(userRule);

        // MCP 工具调用限流规则
        FlowRule mcpRule = new FlowRule();
        mcpRule.setResource("invokeTool");
        mcpRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        mcpRule.setCount(50); // QPS 限制为 50
        mcpRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
        mcpRule.setWarmUpPeriodSec(10);
        rules.add(mcpRule);

        FlowRuleManager.loadRules(rules);
        log.info("限流规则初始化完成，共 {} 条", rules.size());
    }

    /**
     * 初始化熔断降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 用户服务熔断规则
        DegradeRule userDegradeRule = new DegradeRule();
        userDegradeRule.setResource("getUserDTOById");
        userDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO); // 异常比例
        userDegradeRule.setCount(0.5); // 异常比例阈值 50%
        userDegradeRule.setTimeWindow(30); // 熔断时长 30 秒
        userDegradeRule.setMinRequestAmount(10); // 最小请求数
        userDegradeRule.setStatIntervalMs(10000); // 统计时长 10 秒
        rules.add(userDegradeRule);

        // MCP 工具调用熔断规则
        DegradeRule mcpDegradeRule = new DegradeRule();
        mcpDegradeRule.setResource("invokeTool");
        mcpDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        mcpDegradeRule.setCount(0.5);
        mcpDegradeRule.setTimeWindow(30);
        mcpDegradeRule.setMinRequestAmount(5);
        mcpDegradeRule.setStatIntervalMs(10000);
        rules.add(mcpDegradeRule);

        // Feign 调用熔断规则（通用）
        DegradeRule feignDegradeRule = new DegradeRule();
        feignDegradeRule.setResource("ProjectServiceClient");
        feignDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        feignDegradeRule.setCount(0.5);
        feignDegradeRule.setTimeWindow(30);
        feignDegradeRule.setMinRequestAmount(5);
        feignDegradeRule.setStatIntervalMs(10000);
        rules.add(feignDegradeRule);

        DegradeRuleManager.loadRules(rules);
        log.info("熔断降级规则初始化完成，共 {} 条", rules.size());
    }
}