package com.yupi.springbootinit.model.dto.post;

import com.baomidou.mybatisplus.annotation.*;
import com.yupi.springbootinit.model.entity.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName post
 */

@Data
public class PostVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 帖子的正文
     */
    private String content;

    /**
     * 点赞数量
     */
    private Integer thumb;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 发表帖子的用户
     */
    private User createUser;
    private static final long serialVersionUID = 1L;
}