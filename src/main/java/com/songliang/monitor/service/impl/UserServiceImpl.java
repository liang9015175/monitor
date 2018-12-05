package com.songliang.monitor.service.impl;

import com.songliang.monitor.dto.UserDO;
import com.songliang.monitor.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by qiguoliang on 2018/11/30
 * just for demo
 * <p>
 *   1.用户服务类,包含需要被监控的方法
 * <p>
 */
@Service
public class UserServiceImpl implements UserService {

    /**
     * 模拟需要被监控的方法
     * @param id 用户ID
     * @return  用户实体
     */
    public UserDO getUserById(Long id) {
        //随机数生成器,用户模拟接口耗时
        ThreadLocalRandom random=ThreadLocalRandom.current();
        UserDO user = new UserDO();
        user.setId(System.currentTimeMillis());
        user.setName("name:" + System.currentTimeMillis());
        int waitTime = random.nextInt(100,500);//模拟
        System.out.println(waitTime);
        try {
            Thread.sleep(waitTime);//模拟接口耗时
            if(waitTime>400&&waitTime<500){
                //当等待时间介于2000-3000之间的时候,模拟系统发生异常
                throw new RuntimeException("get exception for timeout");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getLocalizedMessage());
        }
        return user;
    }


    public Long addUser(UserDO user) {
        //TODO mock code for com.songliang.monitor.aspect.service
        if (user == null) {
            return System.currentTimeMillis();
        }
        return user.getId();
    }
}
