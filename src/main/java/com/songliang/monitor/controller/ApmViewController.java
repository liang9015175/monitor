package com.songliang.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.songliang.monitor.dto.ApmDto;
import com.songliang.monitor.dto.MonitorLog;
import com.songliang.monitor.enums.DateTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @program: monitor
 * @description: apm可视化数据采集
 * @author: liang.song
 * @create: 2018-12-04-13:19
 **/
@RestController
@RequestMapping("/monitor")
@Api(tags = "监控")
public class ApmViewController {
    /**
     * 获取可视化数据
     * 1.读取monitor.log日志文件
     * 2.解析日志文件到一个list/map结构中
     * 3.遍历list,查找符合要求的集合
     * 4.汇总信息
     */
    @GetMapping(value = "/get")
    @ApiOperation(value = "监控数据获取")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dateType", value = "日期类型(1:最近5分钟 2:最近一个小时 3:最近一天)", required = false, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "method", value = "方法名", required = false, dataType = "long", paramType = "query"),
    })
    public List<ApmDto> readMonitorStatics(@RequestParam(required = false) Integer dateType,@RequestParam(required = false) String method){
        List<ApmDto> list=new ArrayList<>();//结果
        Map<String,List<MonitorLog>> methodMap=new HashMap<>();//存放监控方法名->调用队列
        LocalDateTime startTime=LocalDateTime.now();//开始时间
        LocalDateTime endTime=LocalDateTime.now();//结束时间
        LocalDateTime firstLineTime=null;//找到的第一行记录的时间,用户计算duration
        switch (DateTypeEnum.codeOf(dateType)){
            case last_5_min:
                startTime= LocalDateTime.now().minus(5, ChronoUnit.MINUTES);
                break;
            case last_1_hour:
                startTime=LocalDateTime.now().minusHours(1);
                break;
            case last_1_day:
                startTime=LocalDate.now().atStartOfDay();
            break;
        }
        try {
            //读取监控日志信息
            RandomAccessFile monitorLog = new RandomAccessFile("/Users/songliang/Downloads/monitor/monitor.log", "rw");
            String line=null;
            while ((line=monitorLog.readLine())!=null) {
                String substring = line.substring(0, 19);//根据日期取
                LocalDateTime startDate = LocalDateTime.parse(substring, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                if(startDate.compareTo(startTime)>=1){
                    MonitorLog log=null;
                    if(StringUtils.isEmpty(method)){
                        log= JSON.parseObject(line.substring(20), MonitorLog.class);
                        if(firstLineTime==null){
                            firstLineTime=startDate;
                        }
                    }else {
                        if(line.contains(method)){
                            log= JSON.parseObject(line.substring(20), MonitorLog.class);
                            if(firstLineTime==null){
                                firstLineTime=startDate;
                            }

                        }else {
                            if(firstLineTime==null){
                                firstLineTime=startTime;

                            }
                        }
                    }

                    if(log!=null){
                        List<MonitorLog> logs = methodMap.getOrDefault(log.getMethodName(), new ArrayList<>());
                        logs.add(log);
                        methodMap.put(log.getMethodName(),logs);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(methodMap.isEmpty()){
            return list;
        }

        LocalDateTime finalFirstLineTime = firstLineTime;
        methodMap.forEach((methodName, logs) -> {
            List<String> exceptions = new ArrayList<>();
            AtomicReference<Integer> count = new AtomicReference<>(0);
            AtomicReference<Integer> totalConsume = new AtomicReference<>(0);
            logs.forEach(vv -> {
                Integer timeConsume = Integer.valueOf(vv.getTimeConsume());
                totalConsume.updateAndGet(v1 -> v1 + timeConsume);
                count.getAndSet(count.get() + 1);
                String exception = vv.getException();
                if (!StringUtils.isEmpty(exception)) {
                    exceptions.add(exception);
                }

            });
            long minuteDuration = Duration.between(finalFirstLineTime, endTime).toMinutes();
            minuteDuration=minuteDuration==0?1:minuteDuration;
            long secondDuration = Duration.between(finalFirstLineTime, endTime).toMillis() / 1000;
            secondDuration=secondDuration==0?1:secondDuration;
            ApmDto build = ApmDto.builder()
                    .count(count.get())
                    .endTime(Date.from(endTime.toInstant(ZoneOffset.of("+8"))))
                    .startTime(Date.from(finalFirstLineTime.toInstant(ZoneOffset.of("+8"))))
                    .exceptions(exceptions)
                    .failCount(exceptions.size())
                    .successCount(count.get() - exceptions.size())
                    .methodName(methodName)
                    .successRation(new BigDecimal(count.get() - exceptions.size()).divide(new BigDecimal(count.get()), 2, BigDecimal.ROUND_HALF_UP)).
                            avgMinuteCount(new BigDecimal(count.get()).divide(BigDecimal.valueOf(minuteDuration), 2, BigDecimal.ROUND_HALF_UP))
                    .avgSecondCount(BigDecimal.valueOf(count.get()).divide(BigDecimal.valueOf(secondDuration), 2, BigDecimal.ROUND_HALF_UP))
                    .avgTimeout(BigDecimal.valueOf(totalConsume.get()).divide(BigDecimal.valueOf(count.get()), 2, BigDecimal.ROUND_HALF_UP)).build();
            list.add(build);
        });
        return list;
    }

}
