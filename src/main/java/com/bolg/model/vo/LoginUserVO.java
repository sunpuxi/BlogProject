package com.bolg.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 已登录用户视图（脱敏）
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 **/
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户 id
     */
    private Long id;
    /**
     * 用户账户
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 用户邮箱
     */

    private String email;
    /**
     * 用户手机号
     */

    private String phone;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 标签
     */
    private String tags;

    private static final long serialVersionUID = 1L;
}