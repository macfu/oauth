package com.macfu.oauth.service;

import com.macfu.oauth.po.Member;

/**
 * @Author: liming
 * @Date: 2018/12/24 14:17
 * @Description: 暴露外部的接口Authz
 */
public interface IMemberAuthzService {
    /**
     * 根据用户id获得一个完整的对象信息，包含有密码和锁定状态
     * @param mid
     * @return 用户对象存在返回，不存在返回null
     */
    Member get(String mid);
}
