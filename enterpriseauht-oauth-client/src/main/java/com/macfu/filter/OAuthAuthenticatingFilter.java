package com.macfu.filter;

import com.macfu.filter.token.OAuthToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @Author: liming
 * @Date: 2018/12/26 14:30
 * @Description:
 */
public class OAuthAuthenticatingFilter extends AuthenticatingFilter {
    private String authcodeParam = "code";
    private String failerUrl;

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        // 要传入一个自定义的Token信息
        OAuthToken token = new OAuthToken(servletRequest.getParameter(this.authcodeParam));
        // 设置记住我的功能
        token.setRememberMe(true);
        return token;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        // 在程序之中进行关于oauth登录处理的相关配置操作
        // 获得错误的提示信息
        String error = servletRequest.getParameter("error");
        // 如果出现有错误信息
        if (!(error == null || "".equals(error))) {
            // 错误信息
            String errorDesc = servletRequest.getParameter("error_description");
            // 此时出现有错误信息，则直接跳转到错误页面
            WebUtils.issueRedirect(servletRequest, servletResponse, this.failerUrl + "?error=" + error + "&error_description" + errorDesc);
            // 后续的操作不再执行，而直接进行跳转处理
            return false;
        }
        // 获得subject对象
        Subject subject = super.getSubject(servletRequest, servletResponse);
        // 用户未进行登录认证
        if (!subject.isAuthenticated()) {
            // 接收返回的code信息
            String code = servletRequest.getParameter(this.authcodeParam);
            if (code == null || "".equals(code)) {
                // 跳转到登录页面
                super.saveRequestAndRedirectToLogin(servletRequest, servletResponse);
                return false;
            }
        }
        // 执行登录处理逻辑
        return super.executeLogin(servletRequest, servletResponse);
    }

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        // 跳转到登录成功页面
        super.issueSuccessRedirect(request, response);
        return false;
    }

    @Override
    // 登录失败的处理
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        // 获取当前用户的subject
        Subject subject = super.getSubject(request, response);
        if (subject.isAuthenticated() || subject.isRemembered()) {
            try {
                // 已经成功登录了就返回到首页上
                super.issueSuccessRedirect(request, response);
            } catch (Exception e1){

            }
        } else {
            try {
                // 如果没有登录成功就跳转到失败页面上
                WebUtils.issueRedirect(request, response, this.failerUrl);
            } catch (IOException e1) {

            }
        }
        return false;
    }

    public void setAuthcodeParam(String authcodeParam) {
        this.authcodeParam = authcodeParam;
    }

    public void setFailerUrl(String failerUrl) {
        this.failerUrl = failerUrl;
    }
}
