package com.macfu.oauth.dao;

import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @Author: liming
 * @Date: 2018/12/21 10:49
 * @Description:
 */
public interface IActionDAO {
    Set<String> findaAllByMember(@Param("mid") String mid);
}
