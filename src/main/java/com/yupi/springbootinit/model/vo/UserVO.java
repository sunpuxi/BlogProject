package com.yupi.springbootinit.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户视图（脱敏）
 */
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;



    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    //private String userRole;

    /**
     * 用户邮箱
      */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 标签（JSON字符串）
     */
    private String tags;

    private static final long serialVersionUID = 1L;
}