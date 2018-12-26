package com.macfu.filter.token;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * @Author: liming
 * @Date: 2018/12/26 11:37
 * @Description: 方便实现记住我的功能
 */
public class OAuthToken extends UsernamePasswordToken {
    private String principal;
    private String authcode;

    public OAuthToken(String authcode) {
        this.authcode = authcode;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public void setAuthcode(String authcode) {
        this.authcode = authcode;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return this.authcode;
    }
}
