package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.team.TeamAddRequest;
import com.yupi.springbootinit.model.dto.team.TeamJoinRequest;
import com.yupi.springbootinit.model.dto.team.TeamQueryRequest;
import com.yupi.springbootinit.model.dto.team.TeamUpdateRequest;
import com.yupi.springbootinit.model.entity.Team;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.TeamUserVO;
import com.yupi.springbootinit.model.vo.TeamVO;
import com.yupi.springbootinit.service.TeamService;
import com.yupi.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 队伍接口
 */
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    /**
     * 创建
     *
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest team, HttpServletRequest httpServletRequest) {
        //首先校验数据
        ThrowUtils.throwIf(team == null, ErrorCode.PARAMS_ERROR);
        //判断数据库之前是否存在同名的队伍信息
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", team.getName());
        List<Team> list = teamService.list(queryWrapper);
        ThrowUtils.throwIf(list.size()!=0, ErrorCode.ALREADY_EXITS);
        Team team1 = new Team();
        BeanUtils.copyProperties(team,team1);
        //获取当前登录用户的信息，再添加至创建用户Id的字段即可
        User loginUser = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR);
        team1.setUserId(loginUser.getId());
        //向数据库插入信息
        long l = teamService.addTeam(team1, loginUser);
        ThrowUtils.throwIf(l<=0,ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(l);
    }

    /**
     * 删除
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(long id) {
        ThrowUtils.throwIf(id<0,ErrorCode.PARAMS_ERROR);
        boolean b = teamService.removeById(id);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新  （仅管理员和队伍的创建人可以修改信息）
     * @param teamUpdateRequest
     * @return
     */
    // TODO 解决BUG：用户id与队伍创建者的id一致，却报错无权限
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request) {
        ThrowUtils.throwIf(teamUpdateRequest==null,ErrorCode.PARAMS_ERROR);
        boolean b = teamService.updateTeam(teamUpdateRequest,request);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<TeamVO> getTeamVOById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        TeamVO teamVO = new TeamVO();
        BeanUtils.copyProperties(team,teamVO);
        return ResultUtils.success(teamVO);
    }


    /**
     * 获取队伍信息
     * @param teamQueryRequest
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> ListTeams(TeamQueryRequest teamQueryRequest,HttpServletRequest request){
        ThrowUtils.throwIf(teamQueryRequest==null,ErrorCode.PARAMS_ERROR);
        List<TeamUserVO> teamList = teamService.listTeams(teamQueryRequest,request);
        return ResultUtils.success(teamList);
    }

    /**
     * 分页获取队伍信息
     * @param teamQueryRequest
     * @return
     */
    @GetMapping ("/list/page")
    public BaseResponse<Page<Team>> ListPageTeam( TeamQueryRequest teamQueryRequest){
        ThrowUtils.throwIf(teamQueryRequest==null,ErrorCode.PARAMS_ERROR);
        int pageSize = teamQueryRequest.getPageSize();
        int current = teamQueryRequest.getCurrent();
        Team team = new Team();
        BeanUtils.copyProperties(teamQueryRequest,team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page = new Page<>(current,pageSize);
        Page<Team> page1 = teamService.page(page, queryWrapper);
        return ResultUtils.success(page1);
    }

    /**
     * 用户加入队伍
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        ThrowUtils.throwIf(teamJoinRequest==null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean flag = teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(true);
    }
}
