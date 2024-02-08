package com.yupi.springbootinit.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.service.UserService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 实现缓存预热
 */
@Component
public class PreCache {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 多个线程抢占锁时，因为任务只需要执行一次，所以线程的等待时间应该设置为0；
     */
    @Scheduled(cron = "0 4 0 * * *")
    public void doCacheCommendUser() {
        RLock lock = redissonClient.getLock("findFriends:doPreCache:lock");
        try {
            if (lock.tryLock(0,3,TimeUnit.SECONDS)){
                //查缓存，如果缓存中存在数据，则将数据直接返回，不存在则查数据库
                ValueOperations valueOperations = redisTemplate.opsForValue();
                String cacheKey = "find:user:%s";
                //缓存中没有数据，查数据库,查到数据之后，并写入缓存
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                Page<User> page = userService.page(new Page<>(1, 8), queryWrapper);
                Page<UserVO> pageList = new Page<>();
                BeanUtils.copyProperties(page,pageList);
                valueOperations.set(cacheKey,pageList,10, TimeUnit.MINUTES);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            //释放锁的逻辑应该写到finally中，因为如果try中的代码报错时，就不会执行到unlock；
            lock.unlock();
        }

    }
}
