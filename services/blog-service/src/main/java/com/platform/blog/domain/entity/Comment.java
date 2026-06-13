package com.platform.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.core.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_comment")
public class Comment extends BaseEntity {

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 父评论ID, 0为顶级评论
     */
    private Long parentId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 状态: 1-正常, 0-隐藏
     */
    private Integer status;
}
