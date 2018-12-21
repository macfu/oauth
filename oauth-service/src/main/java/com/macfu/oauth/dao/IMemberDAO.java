package com.macfu.oauth.dao;

import com.macfu.oauth.po.Member;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: liming
 * @Date: 2018/12/21 10:32
 * @Description:
 */
public interface IMemberDAO {
    public Member findById(@Param("mid") String mid);
}
