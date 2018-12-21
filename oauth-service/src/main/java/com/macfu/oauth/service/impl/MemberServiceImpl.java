package com.macfu.oauth.service.impl;

import com.macfu.oauth.dao.IActionDAO;
import com.macfu.oauth.dao.IMemberDAO;
import com.macfu.oauth.dao.IRoleDAO;
import com.macfu.oauth.po.Member;
import com.macfu.oauth.service.IMemberService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author: liming
 * @Date: 2018/12/21 11:14
 * @Description: memberService实现类
 */
@Service
public class MemberServiceImpl implements IMemberService {
    @Resource
    private IMemberDAO memberDAO;
    @Resource
    private IRoleDAO roleDAO;
    @Resource
    private IActionDAO actionDAO;

    @Override
    public Member get(String mid) {
        return memberDAO.findById(mid);
    }

    @Override
    public Map<String, Set<String>> getRoleAndActionByMember(String mid) {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("allRoles", this.roleDAO.findAllByMember(mid));
        map.put("allActions", this.actionDAO.findaAllByMember(mid));
        return map;
    }
}
