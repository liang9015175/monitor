package com.songliang.monitor.console;

import com.songliang.monitor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.*;

/**
 * @program: monitor
 * @description: 定时任务访问接口,便于输出监控日志
 * @author: liang.song
 * @create: 2018-12-05-15:42
 **/
@Service
public class ScheduleService {
    @Autowired
    private UserService userService;
    @Autowired
    ScheduleService(){
        Random random=new Random();
        Executors.newScheduledThreadPool(10).scheduleAtFixedRate(new Runnable(){

            @Override
            public void run() {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Future<Boolean> future = executorService.submit(() -> {
                    userService.getUserById(random.nextInt() * 1L);
                    return true;
                });
                try{
                    future.get(5,TimeUnit.SECONDS);
                }catch (Exception e){
                    System.out.println("exception");
                }finally {
                    executorService.shutdown();
                }

            }
        },0,2,TimeUnit.SECONDS);

    }

}
