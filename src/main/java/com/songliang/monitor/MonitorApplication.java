package com.songliang.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: monitor
 * @description: 监控启动类
 * @author: liang.song
 * @create: 2018-11-30-17:53
 **/
@SpringBootApplication(scanBasePackages = {"com.songliang.monitor"})
public class MonitorApplication {
    @Autowired

    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);

    }

}
