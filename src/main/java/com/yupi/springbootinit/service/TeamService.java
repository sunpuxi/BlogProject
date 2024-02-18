package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.dto.team.TeamJoinRequest;
import com.yupi.springbootinit.model.dto.team.TeamQueryRequest;
import com.yupi.springbootinit.model.dto.team.TeamUpdateRequest;
import com.yupi.springbootinit.model.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.TeamUserVO;
import com.yupi.springbootinit.model.vo.TeamVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 获取队伍的详细信息
     * @param teamQueryRequest
     * @return
     */
    List<TeamUserVO> listTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request);

    /**
     * 更新队伍信息
     * @param teamUpdateRequest
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,HttpServletRequest request);


    /**
     * 用户加入队伍
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);
}
