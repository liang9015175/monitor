package com.songliang.monitor;

import com.songliang.monitor.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by qiguoliang on 2018/11/30
 * <p>
 * <p>
 * </p>
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackages = {"com.songliang.monitor"})
public class UserServiceTest {


    @Autowired
    protected UserService userService;

    @Test
    public void testService() {

        for (int i = 0; i < 10; i++) {
            final int ii=i;
            new Thread(() -> userService.getUserById(Long.valueOf(ii))).start();
        }
        try {
            Thread.sleep(60*60*1000L);//主线程休息
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
