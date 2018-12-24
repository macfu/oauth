package com.macfu.oauth.service.impl;

import com.macfu.oauth.dao.IMemberDAO;
import com.macfu.oauth.po.Member;
import com.macfu.oauth.service.IMemberAuthzService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: liming
 * @Date: 2018/12/21 11:14
 * @Description: memberService实现类
 */
@Service
public class MemberAuthzServiceImpl implements IMemberAuthzService {
    @Resource
    private IMemberDAO memberDAO;

    @Override
    public Member get(String mid) {
        return this.memberDAO.findById(mid);
    }
}
