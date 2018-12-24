package com.macfu.oauth.dao;

import com.macfu.oauth.po.Member;
import com.macfu.oauth.service.IMemberAuthzService;

import javax.annotation.Resource;

/**
 * @Author: liming
 * @Date: 2018/12/24 14:30
 * @Description:
 */
public class MemberAuthzServiceImpl implements IMemberAuthzService {
    @Resource
    private IMemberDAO memberDAO;

    @Override
    public Member get(String mid) {
        return this.memberDAO.findById(mid);
    }
}
