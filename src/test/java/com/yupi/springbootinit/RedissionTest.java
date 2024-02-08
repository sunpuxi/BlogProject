package com.yupi.springbootinit;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

//@SpringBootTest
public class RedissionTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    void testRedisson(){
        RList<String> listTest = redissonClient.getList("list_test");
        //listTest.add("12345678");
        boolean listTest1 = listTest.remove("12345678");
        System.out.println(listTest1);
        //System.out.println("list:"+listTest);
    }
}
