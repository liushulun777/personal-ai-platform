-- 补建 biz_article_chunk 表（文章分块向量表）
-- 如果已存在则跳过

CREATE TABLE IF NOT EXISTS biz_article_chunk (
    id              BIGINT          PRIMARY KEY,
    article_id      BIGINT          NOT NULL,
    chunk_index     INT             NOT NULL,
    content         TEXT            NOT NULL,
    embedding       vector(768),
    author_id       BIGINT          NOT NULL,
    category_id     BIGINT,
    tags            VARCHAR(500),
    publish_time    TIMESTAMP,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_article_chunk_article ON biz_article_chunk(article_id);
CREATE INDEX IF NOT EXISTS idx_article_chunk_author ON biz_article_chunk(author_id);
CREATE INDEX IF NOT EXISTS idx_article_chunk_category ON biz_article_chunk(category_id);
CREATE INDEX IF NOT EXISTS idx_article_chunk_publish_time ON biz_article_chunk(publish_time);

COMMENT ON TABLE biz_article_chunk IS '文章分块向量表';
COMMENT ON COLUMN biz_article_chunk.article_id IS '文章ID';
COMMENT ON COLUMN biz_article_chunk.chunk_index IS '分块序号';
COMMENT ON COLUMN biz_article_chunk.content IS '分块内容';
COMMENT ON COLUMN biz_article_chunk.embedding IS '向量嵌入 (pgvector, 维度768)';
COMMENT ON COLUMN biz_article_chunk.author_id IS '作者ID（冗余字段，用于多维筛选）';
COMMENT ON COLUMN biz_article_chunk.category_id IS '分类ID（冗余字段，用于多维筛选）';
COMMENT ON COLUMN biz_article_chunk.tags IS '标签列表JSON（冗余字段，用于多维筛选）';
COMMENT ON COLUMN biz_article_chunk.publish_time IS '发布时间（冗余字段，用于日期范围筛选）';
