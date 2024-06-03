package com.bolg.service;

import com.bolg.model.entity.UserTeam;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service
* @createDate 2024-02-06 20:19:58
*/
public interface UserTeamService extends IService<UserTeam> {

    /**
     * 根据teamId删除数据
     * @param id
     * @return
     */
    boolean deleteTeams(long id);
}
