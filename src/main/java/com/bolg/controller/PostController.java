package com.bolg.controller;

import com.bolg.common.BaseResponse;
import com.bolg.common.ErrorCode;
import com.bolg.common.ResultUtils;
import com.bolg.exception.ThrowUtils;
import com.bolg.model.DeleteRequest;
import com.bolg.model.dto.post.PostAddRequest;
import com.bolg.model.dto.post.PostUpdateRequest;
import com.bolg.model.dto.post.PostVo;
import com.bolg.model.entity.User;
import com.bolg.service.PostService;
import com.bolg.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/post")
@CrossOrigin(origins = {"https://codefriends.icu","http://codefriends.icu"})  //配置跨域
/**
 * 帖子接口
 */
public class PostController {

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    /**
     * 添加帖子功能
     * @param postAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse addPost(@RequestBody PostAddRequest postAddRequest, HttpServletRequest request){
        ThrowUtils.throwIf(postAddRequest == null, ErrorCode.PARAMS_ERROR,"帖子内容不能为空！");
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(loginUser.getId()==null || loginUser.getId()<0,ErrorCode.NOT_LOGIN_ERROR);
        boolean flag = postService.addPost(postAddRequest,loginUser.getId());
        ThrowUtils.throwIf(!flag,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新帖子
     * @param postUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse updatePost(@RequestBody PostUpdateRequest postUpdateRequest, HttpServletRequest request){
        ThrowUtils.throwIf(postUpdateRequest == null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(loginUser.getId()==null || loginUser.getId()<0,ErrorCode.NOT_LOGIN_ERROR);
        boolean flag = postService.updatePost(postUpdateRequest,loginUser.getId());
        ThrowUtils.throwIf(!flag,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除帖子
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse deletePost(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        ThrowUtils.throwIf(deleteRequest == null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(deleteRequest.getId() == null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(loginUser.getId()==null || loginUser.getId()<0,ErrorCode.NOT_LOGIN_ERROR);
        boolean flag = postService.deletePost(deleteRequest.getId(),loginUser.getId());
        ThrowUtils.throwIf(!flag,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 对帖子的全查询
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<PostVo>> listPost(){
        List<PostVo> list = postService.getPostVo();
        return ResultUtils.success(list);
    }

    /**
     * 帖子点赞功能
     * @return
     */
    @GetMapping("/thumb")
    public BaseResponse thumbPost(Long postId,HttpServletRequest request){
        ThrowUtils.throwIf(request == null,ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(postId <0,ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null||loginUser.getId()<0,ErrorCode.NOT_LOGIN_ERROR);
        boolean flag = postService.thumbPost(loginUser.getId(),postId);
        ThrowUtils.throwIf(!flag,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据帖子内容搜索帖子
     * @param content
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<PostVo>> searchPost(String content){
        ThrowUtils.throwIf(StringUtils.isBlank(content),ErrorCode.PARAMS_ERROR,"搜索内容不能为空");
        List<PostVo> postList = postService.searchPost(content);
        return ResultUtils.success(postList);
    }
}
