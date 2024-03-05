package com.yupi.springbootinit.model.dto.post;

import lombok.Data;

@Data
public class PostAddRequest {
    /**
     * 帖子的正文
     */
    private String content;
}
