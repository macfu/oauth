package com.macfu.ssm.filter;

import com.macfu.ssm.utils.cache.RedisCache;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @Author: liming
 * @Date: 2018/12/19 17:10
 * @Description: 自定义过滤器实现剔出操作
 */
public class KickoutSessionControllerFilter extends AccessControlFilter {
    // 踢出之后需要跳转的页面
    private String kickoutUrl;
    // 之前还是之后的踢出(true=踢出之后的，false=踢出之前的)
    private boolean kickoutAfter = false;
    // 最大的Session保存个数
    private int maxSessionCount = 1;
    // 在shiro里面注销的管理由SessionManager负责，所以需要得到SessionManager对象
    private SessionManager sessionManager;
    // 数据需要缓存到Redis数据库之中
    private Cache<Object, Object> cache;

    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        // 该操作需要返回false，否则onAccessDenied()方法不执行
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        // 能够踢出的用户一定是已经成功登录后的用户，如果未登录的用户不应该有操作
        // subject为shiro的Subject
        Subject subject = super.getSubject(servletRequest, servletResponse);
        // 没有登录
        if (!subject.isAuthenticated() && subject.isRemembered()) {
            // 后续正常访问
            return true;
        }
        /*
        // Shiro自己维持了一套自己的Session管理
        // 此时用户已经认证过了，需要接受到这个Session对象，SessionManager需要做强制性注销管理
        // 同事该Session的数据也需要保存在Redis集合里面
        */
        // 获取当前的Session对象
        Session session = subject.getSession();
        // 如果要进行Redis集合保存，那么一定需要提供有一个mid的数据信息
        // 当前用户的id信息
        String mid = (String) subject.getPrincipal();
        // 对于登录的核心控制，需要设置有一个保存队列，而这个保存队列在Reids里面
        // 获取已经存在的集合信息
        Deque<Serializable> allSessions = (Deque<Serializable>) this.cache.get(mid);
        // 当前没有存储过数据
        if (allSessions == null) {
            // 创建新的集合
            allSessions = new LinkedList<Serializable>();
        }
        // 判断当前的Session是否存在于集合之中，因为多个路径都可以使用到此过滤器，数据为空，表示该数据未存储
        if (!allSessions.contains(session.getId()) && session.getAttribute("kickout ") == null) {
            // 将当前的session保存到集合之中
            allSessions.push(session.getId());
            // 将数据重新保存到Redis集合之中
            this.cache.put(mid, allSessions);
        }
        try {
            // 判断是否已经达到了最大的Session保存量
            // 已经超过了最大保存个数
            if (allSessions.size() > this.maxSessionCount) {
                // 保存要强制注销的SessionID
                Serializable kickoutSessionId = null;
                // 如果踢出的是后者
                if (this.kickoutAfter) {
                    // 踢出第一个
                    kickoutSessionId = allSessions.removeFirst();
                } else {
                    // 踢出后面一个
                    kickoutSessionId = allSessions.removeLast();
                }
                // 重新保存一次Session
                this.cache.put(mid, allSessions);
                // 获取即将被踢出的SessionID的对应的Session对象信息
                Session kickoutSession = this.sessionManager.getSession(new DefaultSessionKey(kickoutSessionId));
                // 这个对象还在
                if (kickoutSession != null) {
                    // 被踢出
                    kickoutSession.setAttribute("kickout", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 当前操作的Session需要被踢出
        if (session.getAttribute("kickout") != null) {
            try {
                // 踢出指定的一个Session数据
                subject.logout();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 记录下本次操作的请求
            super.saveRequest(servletRequest);
            WebUtils.issueRedirect(servletRequest, servletResponse, this.kickoutUrl + "?kickmsg=out");
            // 停止后续的服务
            return false;
        }
        return false;
    }

    public void setMaxSessionCount(int maxSessionCount) {
        this.maxSessionCount = maxSessionCount;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setKickoutUrl(String kickoutUrl) {
        this.kickoutUrl = kickoutUrl;
    }

    public void setKickoutAfter(boolean kickoutAfter) {
        this.kickoutAfter = kickoutAfter;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cache = (RedisCache<Object, Object>) cache.get("kickout");
    }
}
