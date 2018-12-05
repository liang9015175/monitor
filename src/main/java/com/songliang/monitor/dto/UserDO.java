package com.songliang.monitor.dto;

import java.io.Serializable;

/**
 * Created by qiguoliang on 2018/11/30
 * <p>
 * <p>
 * </p>
 */
public class UserDO implements Serializable {

    private String name;
    private Long id;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
