package com.songliang.monitor.service;

import com.songliang.monitor.dto.UserDO;

/**
 * Created by qiguoliang on 2018/11/30
 * <p>
 * <p>
 * </p>
 */
public interface UserService {

    UserDO getUserById(Long id);

    Long addUser(UserDO user);
}
