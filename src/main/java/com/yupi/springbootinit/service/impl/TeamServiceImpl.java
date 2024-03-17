package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.mapper.TeamMapper;
import com.yupi.springbootinit.model.dto.team.TeamJoinRequest;
import com.yupi.springbootinit.model.dto.team.TeamQueryRequest;
import com.yupi.springbootinit.model.dto.team.TeamUpdateRequest;
import com.yupi.springbootinit.model.entity.Team;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.entity.UserTeam;
import com.yupi.springbootinit.model.enums.TeamStatusEnum;
import com.yupi.springbootinit.model.vo.TeamUserVO;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.service.TeamService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author Administrator
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-02-06 20:19:44
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    /**
     * 添加队伍
     *
     * @param team      队伍实体
     * @param loginUser 当前登录的用户
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        final long userId = loginUser.getId();
        // 3. 校验信息
        //   1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //   2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //   3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //   4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //   5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        // 6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        // 7. 校验用户最多创建 5 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        // 8. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        // 9. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }


    /**
     * 获取队伍的详细信息
     *
     * @param teamQueryRequest 前端的队伍查询请求参数
     * @return
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        //参数检验
        //ThrowUtils.throwIf(teamQueryRequest == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //将查询参数中的信息取出
        Long id = teamQueryRequest.getId();
        String name = teamQueryRequest.getName();
        String description = teamQueryRequest.getDescription();
        Integer maxNum = teamQueryRequest.getMaxNum();
        Long userId = teamQueryRequest.getUserId();
        Integer status = teamQueryRequest.getStatus();
        //拼接QueryWapper
        if (id != null && id>0) {
            queryWrapper.eq("id", id);
        }
        String searchText = teamQueryRequest.getSearchText();
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(description)) {
            queryWrapper.like("description", description);
        }
        if (maxNum != null && maxNum > 0) {
            queryWrapper.eq("maxNum", maxNum);
        }
        if (userId != null && userId > 0) {
            queryWrapper.eq("userId", userId);
        }
        if (status != null && status > -1) {
            queryWrapper.eq("Status", status);
        }
        List<Team> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        // 不展示已过期的队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> res = new ArrayList<>();
        //关联查询创建人的用户信息(查询到登录的用户信息并将其封装为Vo类型返回)
        Team team = new Team();
        BeanUtils.copyProperties(teamQueryRequest, team);
        for (Team team1 : list) {
            Long userId1 = team1.getUserId();
            if (userId1 != null && userId1 > 0) {
                TeamUserVO teamUserVO = new TeamUserVO();
                User userById = userService.getById(userId1);  //将此对象设置为TeamUserVO中的createUser属性
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(userById, userVO);
                ThrowUtils.throwIf(userById == null, ErrorCode.NOT_FOUND_ERROR);
                teamUserVO.setCreateUser(userVO);
                BeanUtils.copyProperties(team1, teamUserVO);
                teamUserVO.setTeamId(team1.getId());
                res.add(teamUserVO);
            }
        }
        return res;
    }

    /**
     * 更新队伍信息
     *
     * @param teamUpdateRequest
     * @return
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        // 1.参数校验
        ThrowUtils.throwIf(teamUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = teamUpdateRequest.getId();
        Team byId = this.getById(id);
        ThrowUtils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR);
        // 2.判断是否为管理员或者队伍创建者
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        ThrowUtils.throwIf(loginUserId == null, ErrorCode.NOT_LOGIN_ERROR);
        Long createUserId = byId.getUserId();
        ThrowUtils.throwIf(!String.valueOf(loginUserId).equals(String.valueOf(createUserId)) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        //3.如果队伍的状态为加密状态则必须设置密码
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (teamStatusEnum.equals(TeamStatusEnum.SECRET)) {
            ThrowUtils.throwIf(teamUpdateRequest.getPassword() == null, ErrorCode.PARAMS_ERROR, "加密队伍需要设置密码！");
        }
        // 4.对象属性值拷贝
        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, team);
        boolean b = this.updateById(team);
        // 5.向数据库插入信息
        return b;
    }

    /**
     * 用户加入队伍
     *
     * @param teamJoinRequest
     * @return
     */
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        //参数校验
        ThrowUtils.throwIf(teamJoinRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //加入的队伍一定要存在（包含不能超过过期时间），并且不能超过最大人数
        Long teamJoinId = teamJoinRequest.getId();
        ThrowUtils.throwIf(teamJoinId == null || teamJoinId < 0, ErrorCode.NOT_FOUND_ERROR);
        Team TeamById = this.getById(teamJoinId);
        ThrowUtils.throwIf(TeamById == null, ErrorCode.NOT_FOUND_ERROR, "队伍不存在");
        //不能加入私有的队伍
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(TeamById.getStatus());
        ThrowUtils.throwIf(teamStatusEnum.equals(TeamStatusEnum.PRIVATE), ErrorCode.NO_AUTH_ERROR, "不能加入私有的队伍");
        //加入的队伍的密码需要与当前前端请求的密码匹配,加入的队伍时公开的，则不需要密码
        ThrowUtils.throwIf(!teamStatusEnum.equals(TeamStatusEnum.PUBLIC)&&(teamJoinRequest.getPassword() == null || !teamJoinRequest.getPassword().equals(TeamById.getPassword())), ErrorCode.PARAMS_ERROR, "密码不匹配!");
        //不能重复加入已经加入过的队伍
        // TODO 第一次加入队伍时报错不能重复加入队伍，但数据库中却插入了对应的信息(原因:登录用户在创建队伍时，就将队伍信息添加到了userTeam表中)
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.eq("teamId", teamJoinId);
        long count = userTeamService.count(queryWrapper);
        //log.error("数据库中存在userID为："+loginUser.getId()+"TeamId为："+teamJoinId+"的user-team的数据有"+count);
        ThrowUtils.throwIf(count > 0, ErrorCode.NO_AUTH_ERROR, "不能重复加入队伍");
        //加入的队伍总数不能超过五个
        QueryWrapper<UserTeam> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("userId", loginUser.getId());
        long count1 = userTeamService.count(queryWrapper1);
        ThrowUtils.throwIf(count1 > 4, ErrorCode.NO_AUTH_ERROR, "不能加入超过五个队伍");
        //加入数据库
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getId());
        userTeam.setTeamId(TeamById.getId());
        userTeam.setJoinTime(new Date());
        boolean save = userTeamService.save(userTeam);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR);
        return true;
    }

    /**
     * 退出队伍
     *
     * @param
     * @param request
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(Long id, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(id==null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NO_AUTH_ERROR,"未登录");
        ThrowUtils.throwIf(loginUser.getId()==null||loginUser.getId()<0,ErrorCode.NOT_FOUND_ERROR);
        //校验队伍是否已经存在
        Team teamById = this.getById(id);
        ThrowUtils.throwIf(teamById==null||teamById.getId()<0,ErrorCode.NOT_FOUND_ERROR);
        //校验当前登录用户是否已经加入队伍（这里存疑，前端在展示信息时，没加入的队伍就没有退出功能才对）
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",loginUser.getId());
        queryWrapper.eq("teamId",teamById.getId());
        long isJoinTeam = userTeamService.count(queryWrapper);
        ThrowUtils.throwIf(isJoinTeam==0,ErrorCode.NO_AUTH_ERROR,"未加入的队伍不能退出");
        //如果队伍存在
        QueryWrapper<UserTeam> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("teamId",teamById.getId());
        long peopleNum = userTeamService.count(queryWrapper1);
        ThrowUtils.throwIf(peopleNum<0,ErrorCode.NOT_FOUND_ERROR);
        //1、队伍只剩一人，退出后则解散  2、如果是队长退出队伍，直接销毁队伍(需要移除两个数据库表中的信息，Team表，userTeam表)
        boolean remove;
        if (peopleNum==1 || Objects.equals(teamById.getUserId(), loginUser.getId())){
            remove = userTeamService.remove(queryWrapper1);
        }else{
            remove = userTeamService.remove(queryWrapper1);
        }
        boolean b = this.removeById(id);
        ThrowUtils.throwIf(!remove,ErrorCode.SYSTEM_ERROR);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR);
        return true;
    }

    /**
     * 获取我创建的队伍
     *
     * @param loginUser
     * @return
     */
    @Override
    public List<TeamUserVO> listMyJoinTeams(User loginUser) {
        ThrowUtils.throwIf( loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(loginUser.getId() == null || loginUser.getId()<0,ErrorCode.NOT_FOUND_ERROR);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper();
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> list = userTeamService.list(queryWrapper);
        ThrowUtils.throwIf(list == null || list.size() == 0,ErrorCode.NOT_FOUND_ERROR);
        List<TeamUserVO> res = new ArrayList<>();
        for (UserTeam userTeam : list) {
            Long id = userTeam.getTeamId();
            //根据id从team表中获取数据信息
            Team byId = this.getById(id);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(byId,teamUserVO);
            res.add(teamUserVO);
        }
        return res;
    }

    /**
     * 返回用户还未加入的队伍，只推荐十个队伍
     *
     * @param
     * @param userId
     * @return
     */
    @Override
    public List<TeamUserVO> UserIsNotJoin(long userId) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("userId",userId)
                .last("limit 10");
        List<UserTeam> res = userTeamService.list(queryWrapper);
        ThrowUtils.throwIf(res.size() == 0,ErrorCode.NOT_FOUND_ERROR);
        List<TeamUserVO> result = new ArrayList<>();
        for (UserTeam re : res) {
            Team byId = this.getById(re.getTeamId());
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(byId,teamUserVO);
            result.add(teamUserVO);
        }
        return result;
    }

    /**
     * 判断当前用户是否已经加入该队伍
     *
     * @param userId
     * @param teamId
     * @return
     */
    @Override
    public boolean hasJoin(long userId, long teamId) {
        ThrowUtils.throwIf(userId<0||teamId<0,ErrorCode.PARAMS_ERROR,"非法用户");
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        queryWrapper.eq("teamId",teamId);
        UserTeam one = userTeamService.getOne(queryWrapper);
        if (one == null) return false;
        return true;
    }


}




