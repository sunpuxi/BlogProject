package com.bolg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolg.model.entity.UserTeam;
import com.bolg.service.UserTeamService;
import com.bolg.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-02-06 20:19:58
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

    /**
     * 根据teamId删除数据
     *
     * @param id
     * @return
     */
    @Override
    public boolean deleteTeams(long id) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId",id);
        boolean remove = this.remove(queryWrapper);
        return remove;
    }
}




