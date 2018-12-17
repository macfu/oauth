package com.macfu.ssm.realm.matcher;

import com.macfu.util.enctype.PasswordUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;

/**
 * @Author: liming
 * @Date: 2018/12/17 16:43
 * @Description: 定义密码加密处理的密码匹配
 */
public class DefaultCredentialsMatcher extends SimpleCredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        // 在父类中提供有toString()方法可以自动将传递的字符数组密码变为字符串密码
        Object tokenCredentials = PasswordUtils.encode(super.toString(token.getCredentials()));
        Object accountCredentials = super.getCredentials(info);
        return super.equals(tokenCredentials, accountCredentials);
    }
}