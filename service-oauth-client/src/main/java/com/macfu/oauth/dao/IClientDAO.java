package com.macfu.oauth.dao;

import com.macfu.oauth.po.Client;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: liming
 * @Date: 2018/12/21 14:41
 * @Description: clientDAO
 */
public interface IClientDAO {
    Client findByClientId(@Param("cid") String cid);
}
