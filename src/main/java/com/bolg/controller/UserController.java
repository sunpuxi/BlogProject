package com.bolg.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bolg.annotation.AuthCheck;
import com.bolg.common.BaseResponse;
import com.bolg.common.ErrorCode;
import com.bolg.constant.UserConstant;
import com.bolg.exception.BusinessException;
import com.bolg.exception.ThrowUtils;
import com.bolg.model.dto.user.*;
import com.google.gson.Gson;
import com.bolg.common.DeleteRequest;
import com.bolg.common.ResultUtils;
import com.bolg.model.entity.User;
import com.bolg.model.vo.LoginUserVO;
import com.bolg.model.vo.UserVO;
import com.bolg.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.bolg.service.impl.UserServiceImpl.SALT;

/**
 * 用户接口
 *
 */
@RestController
@RequestMapping("/user")
@Slf4j
@CrossOrigin(origins = {"https://codefriends.icu","http://codefriends.icu"})  //配置跨域
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }


    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据标签名查询用户信息
     * @param
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<UserVO>> searchUserByTagList(@RequestParam(required = false) List<String> tagNameList){
        ThrowUtils.throwIf(CollectionUtils.isEmpty(tagNameList),ErrorCode.PARAMS_ERROR);
        List<UserVO> userList = userService.getUserByTagName(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 主页的推荐用户信息,一次性加载的数据过多可以先将数据存储到缓存再到缓存中取数据；
     * 存储数据到Redis中时，一定要设置过期时间
     * @param
     * @return
     */
    @GetMapping("/recommend")
    // todo 解决数据库与Redis的双写一致性的问题
    public BaseResponse<Page<UserVO>> recommendUsers(long pageSize,long pageNum){
        //查缓存，如果缓存中存在数据，则将数据直接返回，不存在则查数据库
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String cacheKey = "find:user:%s";
        Page<UserVO> pageListResult =(Page<UserVO>) valueOperations.get(cacheKey);
        if (pageListResult!=null){
            return ResultUtils.success(pageListResult);
        }
        //缓存中没有数据，查数据库,查到数据之后，并写入缓存
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> page = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        Page<UserVO> pageList = new Page<>();
        BeanUtils.copyProperties(page,pageList);
        valueOperations.set(cacheKey,pageList,10, TimeUnit.MINUTES);
        return ResultUtils.success(pageList);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 更新个人信息
     *使用延迟双删策略，用户更新之后判断用户是否在推荐用户的列表中，不存在则不需要更新缓存
     * 如果存在，则先删除缓存，在更新数据库，然后隔几秒再次删除缓存
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request) {
        //封装前端的更新请求参数，再获取当前登录的用户信息，将更新后的信息拷贝至登录的用户信息中，此时还需要手动设置id，应为id时自动生成的
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //参数校验，标签的长度不能过长
        ThrowUtils.throwIf(StringUtils.isNotBlank(userUpdateMyRequest.getTags()) && userUpdateMyRequest.getTags().length()>32,ErrorCode.PARAMS_ERROR,"标签过长");
        //从更新请求中取出tags字符串，并将其按照逗号进行切割，然后再拼接成["string","a"]的形式
        if (userUpdateMyRequest.getTags()!=null){
            String[] split = userUpdateMyRequest.getTags().split(",");
            Gson gson = new Gson();
            String s = gson.toJson(split);
            userUpdateMyRequest.setTags(s);
        }
        if (StringUtils.isNotBlank(userUpdateMyRequest.getUserPassword())){
            userUpdateMyRequest.setUserPassword(DigestUtils.md5DigestAsHex((SALT + userUpdateMyRequest.getUserPassword()).getBytes()));
        }
        String cacheKey = "find:user:%s";  // Redis 中的用户缓存信息的key
        redisTemplate.delete(cacheKey);
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        //延迟双删的等待时间
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        redisTemplate.delete(cacheKey);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
