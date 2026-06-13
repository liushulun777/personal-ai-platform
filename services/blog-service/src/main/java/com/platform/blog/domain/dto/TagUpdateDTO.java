package com.platform.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新标签DTO
 */
@Data
public class TagUpdateDTO {

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    private String name;

    /**
     * 标签别名
     */
    @NotBlank(message = "标签别名不能为空")
    @Size(max = 50, message = "标签别名长度不能超过50个字符")
    private String slug;

    /**
     * 描述
     */
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;

    /**
     * 颜色
     */
    @Size(max = 20, message = "颜色长度不能超过20个字符")
    private String color;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private Integer status;
}
