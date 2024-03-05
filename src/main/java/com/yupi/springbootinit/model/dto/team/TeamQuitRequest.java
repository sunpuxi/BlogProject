package com.yupi.springbootinit.model.dto.team;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 更新请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class TeamQuitRequest implements Serializable {
    /**
     * 队伍id
     */
    private Long id;
    private static final long serialVersionUID = 1L;
}