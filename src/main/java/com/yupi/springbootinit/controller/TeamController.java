package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.DeleteRequest;
import com.yupi.springbootinit.model.dto.team.*;
import com.yupi.springbootinit.model.entity.Team;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.entity.UserTeam;
import com.yupi.springbootinit.model.vo.TeamUserVO;
import com.yupi.springbootinit.model.vo.TeamVO;
import com.yupi.springbootinit.service.TeamService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 队伍接口
 */
@RestController
@RequestMapping("/team")
@Slf4j
@CrossOrigin(origins = {"http://1.12.52.237","www.codefriends.icu"})  //配置跨域
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService UserTeamService;

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
     * 删除,同时删除team表和userTeam表中的信息
     * @return
     */

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <0,ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        boolean b = teamService.removeById(id);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR);
        boolean flag = UserTeamService.deleteTeams(id);
        ThrowUtils.throwIf(!flag,ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新  （仅管理员和队伍的创建人可以修改信息）
     * @param teamUpdateRequest
     * @return
     */
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
    public BaseResponse<TeamVO> getTeamVOById(Long id) {
        ThrowUtils.throwIf(id == null || id<0,ErrorCode.PARAMS_ERROR);
        Team team = teamService.getById(id);
        ThrowUtils.throwIf(team==null,ErrorCode.NOT_FOUND_ERROR);
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
        // 如果请求参数为空，默认返回所有队伍信息
        List<TeamUserVO> teamList = teamService.listTeams(teamQueryRequest,request);
        for (TeamUserVO teamUserVO : teamList) {
            if (request == null)
                break;
            QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userId",userService.getLoginUser(request).getId());
            queryWrapper.eq("teamId",teamUserVO.getTeamId());
            UserTeam one = UserTeamService.getOne(queryWrapper);
            if (one != null){
                teamUserVO.setJoin(true);
            }else{
                teamUserVO.setJoin(false);
            }
        }
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
        ThrowUtils.throwIf(!flag,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用户退出队伍
     * @return
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(TeamQuitRequest teamQuitRequest,HttpServletRequest request){
         ThrowUtils.throwIf(teamQuitRequest==null,ErrorCode.PARAMS_ERROR);
        boolean flag = teamService.quitTeam(teamQuitRequest.getId(),request);
        ThrowUtils.throwIf(!flag,ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取我创建的队伍
     * @param
     * @param request
     * @return
     */
    @GetMapping("/list/my")
    public BaseResponse<List<TeamUserVO>> ListMyTeams(TeamQueryRequest teamQueryRequest,HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        long id = loginUser.getId();
        ThrowUtils.throwIf(loginUser == null || id<0,ErrorCode.NOT_FOUND_ERROR);
        teamQueryRequest.setUserId(id);
        List<TeamUserVO> teamUserVOS = teamService.listTeams(teamQueryRequest, request);
        return ResultUtils.success(teamUserVOS);
    }

    /**
     * 获取我创建的队伍
     * @param
     * @param request
     * @return
     */
    @GetMapping("/list/myJoin")
    public BaseResponse<List<TeamUserVO>> ListMyJoinTeams(HttpServletRequest request){
        ThrowUtils.throwIf(request == null,ErrorCode.NO_AUTH_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null,ErrorCode.NOT_LOGIN_ERROR);
        List<TeamUserVO> res = teamService.listMyJoinTeams(loginUser);
        return ResultUtils.success(res);
    }


    /**
     * 返回用户还未加入的队伍
     * @param request
     * @return
     */
    @PostMapping("/isJoin")
    public BaseResponse<List<TeamUserVO>> UserIsNotJoin(HttpServletRequest request){
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId()<0,ErrorCode.NOT_LOGIN_ERROR);
        List<TeamUserVO> res = teamService.UserIsNotJoin(loginUser.getId());
        return ResultUtils.success(res);
    }


    /**
     * true 为已加入队伍
     * @param request  用于取出当前的登录用户
     * @param teamId  前端传来的队伍的id
     * @return
     */
    @PostMapping("/hasJoin")
    public boolean HasJoin(HttpServletRequest request,long teamId){
        ThrowUtils.throwIf(teamId<0,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null,ErrorCode.NOT_LOGIN_ERROR);
        long userId = loginUser.getId();
        ThrowUtils.throwIf(userId<0,ErrorCode.OPERATION_ERROR);
        boolean flag = teamService.hasJoin(userId,teamId);
        return flag;
    }
}
