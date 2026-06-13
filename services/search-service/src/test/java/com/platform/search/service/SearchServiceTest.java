package com.platform.search.service;

import com.platform.common.core.result.PageResult;
import com.platform.search.domain.dto.ArticleSearchDTO;
import com.platform.search.domain.entity.ArticleDocument;
import com.platform.search.domain.vo.ArticleSearchVO;
import com.platform.search.repository.ArticleSearchRepository;
import com.platform.search.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 搜索服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("搜索服务测试")
class SearchServiceTest {

    @Mock
    private ArticleSearchRepository articleSearchRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    private ArticleDocument testDocument;

    @BeforeEach
    void setUp() {
        testDocument = new ArticleDocument();
        testDocument.setId(1L);
        testDocument.setTitle("Spring Boot 入门教程");
        testDocument.setSummary("这是一篇关于 Spring Boot 的入门教程");
        testDocument.setContent("Spring Boot 是一个基于 Spring 框架的快速开发工具...");
        testDocument.setAuthorId(1L);
        testDocument.setAuthorName("admin");
        testDocument.setCategoryId(1L);
        testDocument.setCategoryName("技术");
        testDocument.setTags(Arrays.asList("Java", "Spring"));
        testDocument.setStatus(1);
        testDocument.setViewCount(100);
        testDocument.setLikeCount(50);
        testDocument.setFavoriteCount(30);
        testDocument.setCommentCount(20);
        testDocument.setIsTop(0);
        testDocument.setPublishTime(LocalDateTime.now());
        testDocument.setCreateTime(LocalDateTime.now());
        testDocument.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("索引文章 - 成功")
    void indexArticle_Success() {
        // Given
        when(articleSearchRepository.save(any(ArticleDocument.class))).thenReturn(testDocument);

        // When
        searchService.indexArticle(testDocument);

        // Then
        verify(articleSearchRepository, times(1)).save(testDocument);
    }

    @Test
    @DisplayName("批量索引文章 - 成功")
    void indexArticles_Success() {
        // Given
        List<ArticleDocument> documents = Arrays.asList(testDocument);
        when(articleSearchRepository.saveAll(any())).thenReturn(documents);

        // When
        searchService.indexArticles(documents);

        // Then
        verify(articleSearchRepository, times(1)).saveAll(documents);
    }

    @Test
    @DisplayName("删除文章索引 - 成功")
    void deleteArticle_Success() {
        // Given
        doNothing().when(articleSearchRepository).deleteById(anyLong());

        // When
        searchService.deleteArticle(1L);

        // Then
        verify(articleSearchRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("更新文章索引 - 成功")
    void updateArticle_Success() {
        // Given
        when(articleSearchRepository.save(any(ArticleDocument.class))).thenReturn(testDocument);

        // When
        searchService.updateArticle(testDocument);

        // Then
        verify(articleSearchRepository, times(1)).save(testDocument);
    }

    @Test
    @DisplayName("根据状态查询文章 - 成功")
    void findByStatus_Success() {
        // Given
        List<ArticleDocument> documents = Arrays.asList(testDocument);
        when(articleSearchRepository.findByStatus(1)).thenReturn(documents);

        // When
        List<ArticleDocument> result = articleSearchRepository.findByStatus(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Spring Boot 入门教程", result.get(0).getTitle());
    }
}
