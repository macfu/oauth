package com.macfu.oauth.dao;

import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @Author: liming
 * @Date: 2018/12/21 10:52
 * @Description:
 */
public interface IRoleDAO {
    Set<String> findAllByMember(@Param("mid") String mid);
}
