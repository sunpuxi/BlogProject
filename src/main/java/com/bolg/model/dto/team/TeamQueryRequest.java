package com.bolg.model.dto.team;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询请求
 *
 */
@Data
//@EqualsAndHashCode(callSuper = true)
public class TeamQueryRequest implements Serializable {
    /**
     * 队伍id
     */
    private Long id;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id（队长 id）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;
    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    private static final long serialVersionUID = 1L;
}