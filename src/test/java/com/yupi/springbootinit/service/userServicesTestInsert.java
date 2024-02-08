package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试用例：往数据库添加十万条假数据
 */
//@SpringBootTest
public class userServicesTestInsert {

    @Resource
    private UserService userService;

    @Test
    public void testInsert(){
        int num = 100000;
        List<User> list= new ArrayList<>();
        StopWatch stopWatch =new StopWatch();
        stopWatch.start();
        for (int i = 0; i < num; i++) {
            User user =new User();
            user.setUserAccount("fakeData");
            user.setUserPassword("12345678");
            user.setUnionId("123");
            user.setMpOpenId("123");
            user.setUserName("FakeUserName");
            user.setEmail("123456");
            user.setPhone("123456");
            user.setUserAvatar("https://fastly.jsdelivr.net/npm/@vant/assets/ipad.jpeg");
            user.setUserProfile("假的简介");
            user.setTags("Java");
            list.add(user);
        }
        userService.saveBatch(list,5000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
