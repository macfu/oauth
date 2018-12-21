package com.macfu.oauth.service.impl;

import com.macfu.oauth.dao.IClientDAO;
import com.macfu.oauth.po.Client;
import com.macfu.oauth.po.Member;
import com.macfu.oauth.service.IClientService;
import com.macfu.oauth.service.IMemberService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author: liming
 * @Date: 2018/12/21 11:14
 * @Description: clientService实现类
 */
@Service
public class ClientServiceImpl implements IClientService {
    @Resource
    private IClientDAO clientDAO;

    @Override
    public Client getByClientId(String clientId) {
        return this.clientDAO.findByClientId(clientId);
    }
}
