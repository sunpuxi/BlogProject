package com.bolg.model.dto.post;

import lombok.Data;

/**
 * 更新帖子的请求
 */
@Data
public class PostUpdateRequest {
    /**
     * 帖子的正文
     */
    private String content;

    /**
     * 帖子的id
     */
    private Long id;
}
