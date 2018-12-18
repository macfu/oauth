package com.macfu.ssm.filter;

import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @Author: liming
 * @Date: 2018/12/18 14:22
 * @Description: 内置登录的表单操作
 */
public class DefaultFormAuthenticationFilter extends FormAuthenticationFilter {
    // 验证码生成的session属性名称
    private String randname = "rand";
    // 用户表单输入的名称
    private String randparam = "code";

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        // 验证码属于Session，所以应该首先获取HttpSession
        HttpSession session = ((HttpServletRequest) request).getSession();
        // 通过session获取生成的验证码数据
        String rand = (String) session.getAttribute(this.randname);
        if (rand == null || "".equals(rand)) {
            return super.onAccessDenied(request, response);
        }
        // 获取用户输入的验证码
        String code = request.getParameter(this.randparam);
        if (code == null || "".equals(code)) {
            request.setAttribute("error", "验证码不允许为空");
            // 返回true表示拒绝
            return true;
        } else {
            if (!rand.equalsIgnoreCase(code)) {
                request.setAttribute("error", "验证码输入错误");
                return true;
            }
        }
        // 交给后续处理
        return super.onAccessDenied(request, response);
    }

    public void setRandname(String randname) {
        this.randname = randname;
    }

    public void setRandparam(String randparam) {
        this.randparam = randparam;
    }
}
