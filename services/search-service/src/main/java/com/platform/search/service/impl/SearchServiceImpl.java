package com.platform.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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
import com.platform.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public PageResult<ArticleSearchVO> searchArticles(ArticleSearchDTO searchDTO) {
        try {
            // 构建查询
            Query query = buildSearchQuery(searchDTO);

            // 构建排序
            var sort = buildSort(searchDTO);

            // 构建分页
            int page = (searchDTO.getCurrent() != null ? searchDTO.getCurrent().intValue() : 1) - 1; // ES 从 0 开始
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
            // 使用前缀查询获取建议
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
                                        .map(co.elastic.clients.elasticsearch._types.FieldValue::of)
                                        .collect(Collectors.toList())))
                        ));
                    }

                    // 作者过滤
                    if (searchDTO.getAuthorId() != null) {
                        b.filter(f -> f.term(t -> t.field("authorId").value(searchDTO.getAuthorId())));
                    }

                    return b;
                })
        );
    }

    /**
     * 构建排序
     */
    private List<co.elastic.clients.elasticsearch._types.SortOptions> buildSort(ArticleSearchDTO searchDTO) {
        List<co.elastic.clients.elasticsearch._types.SortOptions> sorts = new ArrayList<>();
        SortOrder order = "asc".equals(searchDTO.getSortOrder()) ? SortOrder.Asc : SortOrder.Desc;

        switch (searchDTO.getSortBy()) {
            case "time":
                sorts.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                        .field(f -> f.field("publishTime").order(order))
                ));
                break;
            case "viewCount":
                sorts.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                        .field(f -> f.field("viewCount").order(order))
                ));
                break;
            case "likeCount":
                sorts.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                        .field(f -> f.field("likeCount").order(order))
                ));
                break;
            default: // relevance
                sorts.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                        .score(sc -> sc.order(order))
                ));
                break;
        }

        // 置顶优先
        sorts.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                .field(f -> f.field("isTop").order(SortOrder.Desc))
        ));

        return sorts;
    }
}
