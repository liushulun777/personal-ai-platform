package com.platform.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.platform.common.core.exception.BusinessException;
import com.platform.common.core.result.PageResult;
import com.platform.common.core.result.ResultCode;
import com.platform.search.convert.ArticleSearchConvert;
import com.platform.search.domain.dto.ArticleSearchDTO;
import com.platform.search.domain.entity.ArticleDocument;
import com.platform.search.domain.vo.ArticleSearchVO;
import com.platform.search.repository.ArticleSearchRepository;
import com.platform.search.service.ArticleChunkService;
import com.platform.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ArticleSearchRepository articleSearchRepository;
    private final ArticleSearchConvert articleSearchConvert;
    private final ElasticsearchClient elasticsearchClient;
    private final ArticleChunkService articleChunkService;

    @Override
    public PageResult<ArticleSearchVO> searchArticles(ArticleSearchDTO searchDTO) {
        try {
            // 构建查询（包含日期范围过滤）
            Query query = buildSearchQuery(searchDTO);

            // 构建排序
            var sort = buildSort(searchDTO);

            // 构建分页
            int page = (searchDTO.getCurrent() != null ? searchDTO.getCurrent().intValue() : 1) - 1;
            int size = searchDTO.getSize() != null ? searchDTO.getSize().intValue() : 10;

            // 执行搜索
            SearchResponse<ArticleDocument> response = elasticsearchClient.search(s -> s
                            .index("articles")
                            .query(query)
                            .from(page * size)
                            .size(size)
                            .sort(sort),
                    ArticleDocument.class
            );

            // 转换结果
            List<ArticleSearchVO> records = response.hits().hits().stream()
                    .map(hit -> {
                        ArticleSearchVO vo = articleSearchConvert.documentToVO(hit.source());
                        vo.setScore(hit.score() != null ? hit.score().floatValue() : null);
                        // 提取高亮内容
                        if (hit.highlight() != null) {
                            if (hit.highlight().containsKey("title")) {
                                vo.setTitle(hit.highlight().get("title").get(0));
                            }
                            if (hit.highlight().containsKey("summary")) {
                                vo.setSummary(hit.highlight().get("summary").get(0));
                            }
                            if (hit.highlight().containsKey("content")) {
                                vo.setContentFragment(hit.highlight().get("content").get(0));
                            }
                        }
                        return vo;
                    })
                    .collect(Collectors.toList());

            long total = response.hits().total() != null ? response.hits().total().value() : 0;

            return PageResult.of(records, total, searchDTO.getCurrent().longValue(), searchDTO.getSize().longValue());
        } catch (IOException e) {
            log.error("搜索文章失败", e);
            throw new BusinessException(ResultCode.FAIL, "搜索失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<ArticleSearchVO> semanticSearch(ArticleSearchDTO searchDTO) {
        try {
            if (searchDTO.getKeyword() == null || searchDTO.getKeyword().isEmpty()) {
                throw new BusinessException(ResultCode.FAIL, "语义搜索需要提供关键词");
            }

            // 构建分页
            int page = (searchDTO.getCurrent() != null ? searchDTO.getCurrent().intValue() : 1) - 1;
            int size = searchDTO.getSize() != null ? searchDTO.getSize().intValue() : 10;
            int topK = Math.max(size * 3, 30); // 多取一些用于分页

            // 1. pgvector 语义搜索，获取按相似度排序的文章ID
            List<Long> semanticArticleIds = articleChunkService.semanticSearch(
                    searchDTO.getKeyword(),
                    searchDTO.getAuthorId(),
                    searchDTO.getCategoryId(),
                    searchDTO.getStartDate(),
                    searchDTO.getEndDate(),
                    topK
            );

            if (semanticArticleIds.isEmpty()) {
                return PageResult.of(new ArrayList<>(), 0L,
                        searchDTO.getCurrent().longValue(), searchDTO.getSize().longValue());
            }

            // 2. 用文章ID列表从 ES 获取完整文档（带高亮）
            List<Long> pageIds = semanticArticleIds.stream()
                    .skip((long) page * size)
                    .limit(size)
                    .collect(Collectors.toList());

            if (pageIds.isEmpty()) {
                return PageResult.of(new ArrayList<>(), (long) semanticArticleIds.size(),
                        searchDTO.getCurrent().longValue(), searchDTO.getSize().longValue());
            }

            Query query = Query.of(q -> q
                    .bool(b -> {
                        b.filter(f -> f.terms(t -> t
                                .field("id")
                                .terms(tv -> tv.value(pageIds.stream()
                                        .map(FieldValue::of)
                                        .collect(Collectors.toList())))
                        ));
                        b.filter(f -> f.term(t -> t.field("status").value(1)));
                        return b;
                    })
            );

            SearchResponse<ArticleDocument> response = elasticsearchClient.search(s -> s
                            .index("articles")
                            .query(query)
                            .size(pageIds.size()),
                    ArticleDocument.class
            );

            // 3. 按语义搜索顺序排列结果，并附加相似度分数
            Map<Long, ArticleDocument> docMap = response.hits().hits().stream()
                    .collect(Collectors.toMap(
                            hit -> hit.source().getId(),
                            hit -> hit.source(),
                            (a, b) -> a
                    ));

            List<ArticleSearchVO> records = new ArrayList<>();
            for (int i = 0; i < pageIds.size(); i++) {
                Long articleId = pageIds.get(i);
                ArticleDocument doc = docMap.get(articleId);
                if (doc != null) {
                    ArticleSearchVO vo = articleSearchConvert.documentToVO(doc);
                    // 计算语义相似度分数（基于排名，越靠前越高）
                    double semanticScore = 1.0 - ((double) (page * size + i) / semanticArticleIds.size());
                    vo.setSemanticScore(Math.round(semanticScore * 1000.0) / 1000.0);
                    records.add(vo);
                }
            }

            return PageResult.of(records, (long) semanticArticleIds.size(),
                    searchDTO.getCurrent().longValue(), searchDTO.getSize().longValue());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("语义搜索失败", e);
            throw new BusinessException(ResultCode.FAIL, "语义搜索失败: " + e.getMessage());
        }
    }

    @Override
    public void indexArticle(ArticleDocument document) {
        try {
            articleSearchRepository.save(document);
            log.info("索引文章成功: {}", document.getId());
        } catch (Exception e) {
            log.error("索引文章失败: {}", document.getId(), e);
            throw new BusinessException(ResultCode.FAIL, "索引文章失败: " + e.getMessage());
        }
    }

    @Override
    public void indexArticles(List<ArticleDocument> documents) {
        try {
            articleSearchRepository.saveAll(documents);
            log.info("批量索引文章成功, 数量: {}", documents.size());
        } catch (Exception e) {
            log.error("批量索引文章失败", e);
            throw new BusinessException(ResultCode.FAIL, "批量索引文章失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteArticle(Long articleId) {
        try {
            articleSearchRepository.deleteById(articleId);
            log.info("删除文章索引成功: {}", articleId);
        } catch (Exception e) {
            log.error("删除文章索引失败: {}", articleId, e);
            throw new BusinessException(ResultCode.FAIL, "删除文章索引失败: " + e.getMessage());
        }
    }

    @Override
    public void updateArticle(ArticleDocument document) {
        try {
            articleSearchRepository.save(document);
            log.info("更新文章索引成功: {}", document.getId());
        } catch (Exception e) {
            log.error("更新文章索引失败: {}", document.getId(), e);
            throw new BusinessException(ResultCode.FAIL, "更新文章索引失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getSuggestions(String keyword) {
        try {
            Query query = Query.of(q -> q
                    .multiMatch(m -> m
                            .fields("title", "summary")
                            .query(keyword)
                    )
            );

            SearchResponse<ArticleDocument> response = elasticsearchClient.search(s -> s
                            .index("articles")
                            .query(query)
                            .size(10),
                    ArticleDocument.class
            );

            return response.hits().hits().stream()
                    .map(hit -> hit.source().getTitle())
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("获取搜索建议失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建搜索查询
     */
    private Query buildSearchQuery(ArticleSearchDTO searchDTO) {
        return Query.of(q -> q
                .bool(b -> {
                    // 只搜索已发布的文章
                    b.filter(f -> f.term(t -> t.field("status").value(1)));

                    // 关键词搜索
                    if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                        b.must(m -> m
                                .multiMatch(mm -> mm
                                        .fields("title^3", "summary^2", "content", "tags")
                                        .query(searchDTO.getKeyword())
                                        .fuzziness("AUTO")
                                )
                        );
                    }

                    // 分类过滤
                    if (searchDTO.getCategoryId() != null) {
                        b.filter(f -> f.term(t -> t.field("categoryId").value(searchDTO.getCategoryId())));
                    }

                    // 标签过滤
                    if (searchDTO.getTags() != null && !searchDTO.getTags().isEmpty()) {
                        b.filter(f -> f.terms(t -> t
                                .field("tags")
                                .terms(tv -> tv.value(searchDTO.getTags().stream()
                                        .map(FieldValue::of)
                                        .collect(Collectors.toList())))
                        ));
                    }

                    // 作者过滤
                    if (searchDTO.getAuthorId() != null) {
                        b.filter(f -> f.term(t -> t.field("authorId").value(searchDTO.getAuthorId())));
                    }

                    // 日期范围过滤
                    if (searchDTO.getStartDate() != null || searchDTO.getEndDate() != null) {
                        b.filter(f -> f.range(r -> r.date(d -> {
                            d.field("publishTime");
                            if (searchDTO.getStartDate() != null) {
                                d.gte(toIsoString(searchDTO.getStartDate()));
                            }
                            if (searchDTO.getEndDate() != null) {
                                d.lte(toIsoString(searchDTO.getEndDate()));
                            }
                            return d;
                        })));
                    }

                    return b;
                })
        );
    }

    /**
     * 构建排序
     */
    private List<SortOptions> buildSort(ArticleSearchDTO searchDTO) {
        List<SortOptions> sorts = new ArrayList<>();
        SortOrder order = "asc".equals(searchDTO.getSortOrder()) ? SortOrder.Asc : SortOrder.Desc;

        switch (searchDTO.getSortBy()) {
            case "time":
                sorts.add(SortOptions.of(s -> s
                        .field(f -> f.field("publishTime").order(order))
                ));
                break;
            case "viewCount":
                sorts.add(SortOptions.of(s -> s
                        .field(f -> f.field("viewCount").order(order))
                ));
                break;
            case "likeCount":
                sorts.add(SortOptions.of(s -> s
                        .field(f -> f.field("likeCount").order(order))
                ));
                break;
            default: // relevance
                sorts.add(SortOptions.of(s -> s
                        .score(sc -> sc.order(order))
                ));
                break;
        }

        // 置顶优先
        sorts.add(SortOptions.of(s -> s
                .field(f -> f.field("isTop").order(SortOrder.Desc))
        ));

        return sorts;
    }

    /**
     * LocalDateTime 转 ISO 8601 字符串
     */
    private String toIsoString(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toString();
    }
}
