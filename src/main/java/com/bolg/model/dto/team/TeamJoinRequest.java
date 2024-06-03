package com.bolg.model.dto.team;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 */
@Data
public class TeamJoinRequest implements Serializable {
    /**
     * 队伍id
     */
    private Long id;

    /**
     * 加密队伍的密码
     */
    private String password;

    private static final long serialVersionUID = 1L;
}