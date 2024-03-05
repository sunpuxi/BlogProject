package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.team.TeamQueryRequest;
import com.yupi.springbootinit.model.entity.Team;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.entity.UserTeam;
import com.yupi.springbootinit.model.vo.TeamUserVO;
import com.yupi.springbootinit.service.TeamService;
import com.yupi.springbootinit.service.UserTeamService;
import com.yupi.springbootinit.mapper.UserTeamMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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




