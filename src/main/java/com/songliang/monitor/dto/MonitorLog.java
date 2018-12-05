package com.songliang.monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * @program: monitor
 * @description: 监控日志实体,用户序列化监控日志json
 * @author: liang.song
 * @create: 2018-11-30-17:04
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorLog {
    /**
     * 接口耗时
     */
    private String timeConsume;
    /**
     * 接口参数
     */
    private String args;
    /**
     * 接口名称(方便根据接口维度进行统计)
     */
    private String methodName;

    /**
     * 异常信息
     */
    private String exception;
    /**
     * 创建时间
     */
    private Date  createTime;

}