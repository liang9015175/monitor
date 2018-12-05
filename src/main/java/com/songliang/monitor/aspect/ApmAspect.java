package com.songliang.monitor.aspect;

import com.alibaba.fastjson.JSON;
import com.songliang.monitor.dto.MonitorLog;
import com.songliang.monitor.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @program: monitor
 * @description: 埋点监控切面
 * @author: liang.song
 * @create: 2018-11-30-16:30
 **/
@Aspect
@Component
public class ApmAspect {
    private static final Logger log= LoggerFactory.getLogger("monitorLog");
    /**
     * threadLocal 用户存储方法调用参数信息
     */
    private ThreadLocal<Map<String,Object>> threadLocal=ThreadLocal.withInitial(ConcurrentHashMap::new);
    /**
     * 定义切入点:拦截{@link UserServiceImpl#getUserById(Long)}方法
     */
    @Pointcut("execution(public * com.songliang.monitor.service.impl.*.*(..))")
    public void exec(){

    }

    @Before(value ="exec()")
    public void before(JoinPoint joinPoint){
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        String methodName = methodSignature.getMethod().getName();//方法名
        Object[] args = joinPoint.getArgs();//参数
        if (this.threadLocal.get() == null) {
            this.threadLocal.set(new ConcurrentHashMap<>());
        }
        this.threadLocal.get().put("startTime", LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());//方法调用开始时间
        this.threadLocal.get().put("args",arrayToString(args));
        this.threadLocal.get().put("methodName",methodName);

    }
    @AfterReturning(value = "exec()", returning = "ret")
    public  void afterRet(Object ret){
        //异步记录日志
        timeConsume();
        asyncLog();
    }
    @AfterThrowing(value = "exec()",throwing = "throwable")
    public void afterThrow(Throwable throwable){
        threadLocal.get().put("exception", throwable.getMessage() == null ? "" : throwable.getMessage());
        timeConsume();
        asyncLog();

    }
    private String arrayToString(Object[] args) {
        List<String> collect = Arrays.stream(Optional.ofNullable(args).orElse(new Object[]{}))
                .map(JSON::toJSONString)
                .collect(Collectors.toList());
        return String.join("\' \'", collect);
    }
    /**
     *  记录耗时
     */
    private void timeConsume() {
        long endTime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        threadLocal.get().put("endTime", endTime);
        threadLocal.get().putIfAbsent("startTime", endTime);
        Long startTime = (Long) threadLocal.get().get("startTime");
        long timeConsume = endTime - startTime;
        threadLocal.get().put("timeConsume", Long.valueOf(timeConsume).toString());
    }

    /**
     * 异步记录日志,发送消息到redis中,此处建议日志消息先落地，方式消息发送失败,监控日志丢失
     */
    private void asyncLog() {
        Map<String, Object> map = threadLocal.get();
        String timeConsume = (String) map.getOrDefault("timeConsume", "");
        String args = (String) map.getOrDefault("args", "");
        String exception = (String) map.getOrDefault("exception", "");
        String methodName = (String) map.getOrDefault("methodName", "");
        MonitorLog build = MonitorLog.builder()
                .args(args)
                .exception(exception)
                .methodName(methodName)
                .timeConsume(timeConsume)
                .build();
        log.info(JSON.toJSONString(build,false));
        System.out.println(JSON.toJSONString(build,false));
        //记录日志之后,清空缓存，防止压爆threadLocal
        threadLocal.remove();
    }


}
