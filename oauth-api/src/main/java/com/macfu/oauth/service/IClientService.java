package com.macfu.oauth.service;

import com.macfu.oauth.po.Client;

/**
 * @Author: liming
 * @Date: 2018/12/20 10:55
 * @Description: Client业务接口
 */
public interface IClientService {
    /**
     * 根据clientId(client_id)查找是否存在有客户信息
     * @param clientId
     * @return
     */
    public Client getByClientId(String clientId);
}
