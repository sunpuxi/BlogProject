package com.yupi.springbootinit.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.team.TeamQueryRequest;
import com.yupi.springbootinit.model.entity.Team;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.entity.UserTeam;
import com.yupi.springbootinit.model.enums.TeamStatusEnum;
import com.yupi.springbootinit.model.vo.LoginUserVO;
import com.yupi.springbootinit.model.vo.TeamUserVO;
import com.yupi.springbootinit.model.vo.TeamVO;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.service.TeamService;
import com.yupi.springbootinit.mapper.TeamMapper;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.service.UserTeamService;
import org.apache.lucene.util.CollectionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @author Administrator
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-02-06 20:19:44
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

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
        // TODO 有 bug，可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
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
     * @param teamQueryRequest  前端的队伍查询请求参数
     * @return
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        //参数检验
        ThrowUtils.throwIf(teamQueryRequest==null,ErrorCode.PARAMS_ERROR);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //将查询参数中的信息取出
        Long id = teamQueryRequest.getId();
        String name = teamQueryRequest.getName();
        String description = teamQueryRequest.getDescription();
        Integer maxNum = teamQueryRequest.getMaxNum();
        Long userId = teamQueryRequest.getUserId();
        Integer status = teamQueryRequest.getStatus();
        //拼接QueryWapper
        if (id>0 && id!=null){
            queryWrapper.eq("id",id);
        }
        String searchText = teamQueryRequest.getSearchText();
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(name)){
            queryWrapper.like("name",name);
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(description)){
            queryWrapper.like("description",description);
        }
        if (maxNum!=null && maxNum>0 ){
            queryWrapper.eq("maxNum",maxNum);
        }
        if ( userId!=null&&userId>0 ){
            queryWrapper.eq("userId",userId);
        }
        if (status!=null && status>-1){
            queryWrapper.eq("Status",status);
        }
        List<Team> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)){
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
        BeanUtils.copyProperties(teamQueryRequest,team);
        for (Team team1 : list) {
            Long userId1 = team1.getUserId();
            if( userId1!=null&&userId1>0){
                TeamUserVO teamUserVO = new TeamUserVO();
                User userById = userService.getById(userId1);  //将此对象设置为TeamUserVO中的createUser属性
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(userById,userVO);
                ThrowUtils.throwIf(userById==null,ErrorCode.NOT_FOUND_ERROR);
                teamUserVO.setCreateUser(userVO);
                BeanUtils.copyProperties(team1,teamUserVO);
                res.add(teamUserVO);
            }
        }
        return res;
    }

}




