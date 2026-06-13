package com.platform.ai.service;

import com.platform.ai.service.impl.AiGenerateServiceImpl;
import com.platform.common.ai.service.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * AI生成服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AI生成服务测试")
class AiGenerateServiceTest {

    @Mock
    private AiService aiService;

    @InjectMocks
    private AiGenerateServiceImpl aiGenerateService;

    private String testContent;
    private String testTitle;

    @BeforeEach
    void setUp() {
        testTitle = "Spring Boot 入门教程";
        testContent = "Spring Boot 是一个基于 Spring 框架的快速开发工具，它简化了 Spring 应用的创建和开发过程。";
    }

    @Test
    @DisplayName("生成摘要 - 成功")
    void generateSummary_Success() {
        // Given
        String expectedSummary = "这是一篇关于 Spring Boot 的入门教程";
        when(aiService.chat(anyString())).thenReturn(expectedSummary);

        // When
        String result = aiGenerateService.generateSummary(testContent, 200);

        // Then
        assertNotNull(result);
        assertEquals(expectedSummary, result);
    }

    @Test
    @DisplayName("生成标签 - 成功")
    void generateTags_Success() {
        // Given
        String expectedTags = "Java,Spring,Boot,教程,入门";
        when(aiService.chat(anyString())).thenReturn(expectedTags);

        // When
        List<String> result = aiGenerateService.generateTags(testTitle, testContent, 5);

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
        assertTrue(result.contains("Java"));
        assertTrue(result.contains("Spring"));
    }

    @Test
    @DisplayName("生成标题 - 成功")
    void generateTitles_Success() {
        // Given
        String expectedTitles = "Spring Boot 快速入门\n从零开始学 Spring Boot\nSpring Boot 实战指南";
        when(aiService.chat(anyString())).thenReturn(expectedTitles);

        // When
        List<String> result = aiGenerateService.generateTitles(testContent, 3);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.get(0).contains("Spring Boot"));
    }
}
