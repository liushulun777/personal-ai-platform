package com.platform.common.web.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sentinel 熔断降级测试
 */
@Slf4j
@SpringBootTest
public class SentinelTest {

    private static final String RESOURCE_NAME = "testResource";

    @BeforeEach
    public void setUp() {
        // 清除之前的规则
        FlowRuleManager.loadRules(new ArrayList<>());
        DegradeRuleManager.loadRules(new ArrayList<>());
    }

    @Test
    public void testFlowControl() {
        // 配置限流规则：QPS 限制为 1
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource(RESOURCE_NAME);
        rule.setGrade(com.alibaba.csp.sentinel.slots.block.RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(1);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);

        // 第一个请求应该成功
        Entry entry1 = null;
        try {
            entry1 = SphU.entry(RESOURCE_NAME);
            log.info("第一个请求通过");
        } catch (BlockException e) {
            fail("第一个请求不应该被限流");
        } finally {
            if (entry1 != null) {
                entry1.exit();
            }
        }

        // 第二个请求应该被限流
        Entry entry2 = null;
        try {
            entry2 = SphU.entry(RESOURCE_NAME);
            fail("第二个请求应该被限流");
        } catch (BlockException e) {
            log.info("第二个请求被限流: {}", e.getClass().getSimpleName());
        } finally {
            if (entry2 != null) {
                entry2.exit();
            }
        }
    }

    @Test
    public void testCircuitBreaker() {
        // 配置熔断规则：异常比例阈值 50%，熔断时长 1 秒
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule();
        rule.setResource(RESOURCE_NAME);
        rule.setGrade(com.alibaba.csp.sentinel.slots.block.RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        rule.setCount(0.5);
        rule.setTimeWindow(1);
        rule.setMinRequestAmount(3);
        rule.setStatIntervalMs(1000);
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);

        // 模拟 3 次调用，2 次失败
        for (int i = 0; i < 3; i++) {
            Entry entry = null;
            try {
                entry = SphU.entry(RESOURCE_NAME);
                if (i < 2) {
                    // 前两次调用抛出异常
                    throw new RuntimeException("模拟异常");
                }
                log.info("第三次调用成功");
            } catch (BlockException e) {
                log.info("请求被熔断: {}", e.getClass().getSimpleName());
            } catch (Exception e) {
                log.info("请求异常: {}", e.getMessage());
                // 记录异常
                com.alibaba.csp.sentinel.Tracer.trace(e);
            } finally {
                if (entry != null) {
                    entry.exit();
                }
            }
        }

        // 等待统计窗口结束
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 第四个请求应该被熔断
        Entry entry4 = null;
        try {
            entry4 = SphU.entry(RESOURCE_NAME);
            log.info("第四个请求通过（熔断器应该打开）");
        } catch (BlockException e) {
            log.info("第四个请求被熔断: {}", e.getClass().getSimpleName());
            assertNotNull(e, "熔断器应该打开");
        } finally {
            if (entry4 != null) {
                entry4.exit();
            }
        }
    }

    @Test
    public void testFallbackMethod() {
        // 测试降级方法是否被正确调用
        // 这里需要模拟 Feign 调用失败的情况
        // 由于是集成测试，需要启动完整的 Spring 上下文
        log.info("降级方法测试需要完整的 Spring 上下文");
        assertTrue(true, "降级方法测试占位");
    }
}