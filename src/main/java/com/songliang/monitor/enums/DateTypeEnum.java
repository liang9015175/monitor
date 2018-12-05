package com.songliang.monitor.enums;

import lombok.Getter;

import java.util.Date;

/**
 * @program: monitor
 * @description: ${description}
 * @author: liang.song
 * @create: 2018-12-05-14:12
 **/
@Getter
public enum DateTypeEnum {
    last_5_min(1,"最近5分钟"),last_1_hour(2,"最近1小时"),last_1_day(3,"最近1天");
    private Integer code;
    private String desc;
    DateTypeEnum(Integer code,String desc){
        this.code=code;
        this.desc=desc;
    }
    public static DateTypeEnum codeOf(Integer code){
        for(DateTypeEnum v:values()){
            if(v.code.equals(code)){
                return v;
            }
        }
        throw new RuntimeException("不支持的时间选择");
    }
}
