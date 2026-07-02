package com.platform.report.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 大屏VO
 */
@Data
public class DashboardVO {

    /**
     * 大屏ID
     */
    private Long id;

    /**
     * 大屏名称
     */
    private String name;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 分享链接
     */
    private String shareUrl;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
