package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.User;

/**
* @author Administrator
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-02-06 20:19:44
*/
public interface TeamService extends IService<Team> {
    /**
     * 添加队伍
     * @param team  队伍实体
     * @param loginUser  当前登录的用户
     * @return
     */
    long addTeam(Team team, User loginUser);
}
