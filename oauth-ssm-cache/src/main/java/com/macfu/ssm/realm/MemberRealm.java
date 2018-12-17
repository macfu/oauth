package com.macfu.ssm.realm;

import com.macfu.oauth.po.Member;
import com.macfu.oauth.service.IMemberService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * @Author: liming
 * @Date: 2018/12/17 16:43
 * @Description: 用户角色和权限管理Realm
 */
public class MemberRealm extends AuthorizingRealm {
    private Logger log = LoggerFactory.getLogger(MemberRealm.class);

    @Resource
    private IMemberService memberService;

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 主要是是实现用户的认证处理操作
        System.err.println("===== 1.进行用户的认证处理(doGetAuthenticationInfo) =====");
        // 获取用户名
        String mid = (String) authenticationToken.getPrincipal();
        // 根据用户名查询出用户的完整信息
        Member member = this.memberService.get(mid);
        // 如果用户信息不存在，
        if (member == null) {
            throw new UnknownAccountException("账号" + mid + "不存在");
        }
        // 判断账户是否被锁定
        if (member.getLocked().equals(1)) {
            throw new LockedAccountException(mid + "账号信息已经被锁定，无法登录");
        }
        SecurityUtils.getSubject().getSession().setAttribute("name", member.getName());
        return new SimpleAuthenticationInfo(authenticationToken.getPrincipal(), member.getPassword(), "memberRealm");
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        // 用于用户的授权处理操作，授权一定要要在认证之后进行
        System.err.println("===== 2.进行用户的授权处理操作(doGetAuthorizationInfo) =====");
        // 返回授权信息
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 获取用户名
        String mid = (String) principalCollection.getPrimaryPrincipal();
        Map<String, Set<String>> roleAndActionByMember = this.memberService.getRoleAndActionByMember(mid);
        // 将所有的角色信息保存在授权信息中
        info.setRoles(roleAndActionByMember.get("allRoles"));
        // 保存所有的权限
        info.setStringPermissions(roleAndActionByMember.get("allActions"));
        return info;
    }
}
