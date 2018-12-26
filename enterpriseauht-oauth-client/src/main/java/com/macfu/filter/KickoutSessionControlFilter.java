package com.macfu.filter;

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import cn.mldn.enterpriseauth.ssm.util.cache.RedisCache;

public class KickoutSessionControlFilter extends AccessControlFilter {
	private String kickoutUrl ; // 剔出之后需要跳转的页面
	// kickoutAfter = true：表示踢出之后的
	// kickoutAfter = false：表示保留之后进来的，踢出之前的
	private boolean kickoutAfter = false ; // 之前还是之后剔出
	private int maxSessionCount = 1 ; // 最大的session保存个数
	// 在Shiro里面注销的管理由SessionManager负责，所以需要得到SessionManager对象
	private SessionManager sessionManager ;
	private Cache<Object,Object> cache ; // 数据需要缓存到Redis数据库之中
	
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
			throws Exception {
		return false;	// 该操作要返回false，否则onAccessDenied()方法不执行
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		// 能够踢出的用户一定是在已经成功登录后的用户，如果未登录认证的用户不应该操作
		Subject subject = super.getSubject(request, response) ;// Shiro的Subject
		if (!subject.isAuthenticated() && !subject.isRemembered()) {	// 没有登录
			return true ; // 后续正常访问
		}	// Shiro自己维持了一套自己的Session管理
		// 此时用户已经认证过了，需要接收这个Session对象，SessionManager需要做强制性注销处理
		// 同时该session的数据也需要保存在Redis集合里面
		Session session = subject.getSession() ; // 获得当前的Session对象
		// 如果要进行Redis集合保存，那么一定需要提供有一个mid的数据信息
		String mid = (String) subject.getPrincipal() ;  // 当前用户id信息
		// 对于登录的核心控制，需要设置有一个 保存队列，而这个保存队列在Redis里面
		Deque<Serializable> allSessions = (Deque<Serializable>) this.cache.get(mid) ; // 获取已经存在的集合信息
		if (allSessions == null) {	// 当前没有存储过数据
			allSessions = new LinkedList<Serializable>() ; // 创建新的集合
			
		} // 判断当前的session是否存在于集合之中，因为多个路径都可以使用到此过滤器，数据为空，表示该数据未存储
		if (!allSessions.contains(session.getId()) && session.getAttribute("kickout") == null) { 
			allSessions.push(session.getId()); // 将当前的session保存到集合之中
			this.cache.put(mid, allSessions) ; // 将数据重新保存到Redis集合之中
		}
		try { // 判断是否已经达到了最大的session保存量
			if (allSessions.size() > this.maxSessionCount) {	// 已经超过了最大保存个数
				Serializable kickoutSessionId = null ; // 保存要强制注销的SessionID
				if(this.kickoutAfter) {	// 如果是踢出后者
					kickoutSessionId = allSessions.removeFirst() ; // 踢出第一个
				} else {	// 踢出前一个
					kickoutSessionId = allSessions.removeLast() ; // 踢出后面的一个
				}
				this.cache.put(mid, allSessions) ;// 重新保存一次session
				// 获得即将被提出的SessionID的对应的Session对象信息
				Session kickoutSession = this.sessionManager.getSession(new DefaultSessionKey(kickoutSessionId));
				if (kickoutSession != null) {	// 这个用户还在呢
					kickoutSession.setAttribute("kickout", true); // 你被踢出
				}
			}
		} catch (Exception e) {}
		if (session.getAttribute("kickout") != null) {	// 当前操作的session需要被踢出
			try {
				subject.logout(); // 踢出指定的一个Session数据
			} catch (Exception e) {}
			super.saveRequest(request); // 记录下本次的请求操作
			WebUtils.issueRedirect(request, response, this.kickoutUrl + "?kickmsg=out");
			return false ; // 停止掉后续的访问服务
		}
		return true; // 正确只想你个后续访问服务
	}
	public void setMaxSessionCount(int maxSessionCount) {
		this.maxSessionCount = maxSessionCount;
	}
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	public void setKickoutAfter(boolean kickoutAfter) {
		this.kickoutAfter = kickoutAfter;
	}
	public void setKickoutUrl(String kickoutUrl) {
		this.kickoutUrl = kickoutUrl;
	}
	public void setCacheManager(CacheManager cacheManager) {
		// 由于需要设置一个数据的有效时间，所以不能够使用Cache标准操作，要利用其子类处理
		this.cache = (RedisCache<Object,Object>) cacheManager.getCache("kickout") ;
	}
}
