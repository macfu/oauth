package com.macfu.realm.matcher;

import com.macfu.util.enctype.*;
import com.macfu.util.cache.RedisCache;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.cache.CacheManager;

import java.util.concurrent.atomic.AtomicInteger;
// 定义一个密码加密处理的密码匹配其
public class DefaultCredentialsMatcher extends SimpleCredentialsMatcher {
	private RedisCache<Object,Object> cache ; // 整体的操作一定需要有一个Redis缓存配置项
	private String expire = "50" ; // 默认是50秒失效
	private int maxRetryCount = 5 ; // 最多可以实验5次
	/**
	 * 实现登录计数处理，如果要进行用户的登录计数，那么就应该将用户名作为数据中的key
	 * @param mid 要保存的数据库中的key信息
	 */
	private void retry(String mid) {
		// 之所以使用AtomicInteger原子操作类是为了防止多个用户并发操作时候的不同步问题
		AtomicInteger num = (AtomicInteger) this.cache.get(mid) ; // 获取保存的对象
		System.err.println("************** num = " + num);
		if (num == null) {	// 现在还没有相关的数据，用户没有登录过或者登录成功 
			num = new AtomicInteger(1) ; // 设置一个登录次数
			this.cache.put(mid, num) ; // 保存信息
		}// 如果现在已经保存有登录次数（你至少已经失败过一次）
		if (num.incrementAndGet() > this.maxRetryCount) {	// 超过了最大尝试次数
			this.cache.putEx(mid, num, this.expire) ;// 设置一个失效时间
			throw new ExcessiveAttemptsException("用户“" + mid + "”密码尝试次数过多，请稍后再试！");
		} else {	// 如果现在不够最大次数
			this.cache.put(mid, num) ; // 保存当前尝试次数
		}
	}
	/**
	 * 当用户成功登录之后所有的数据应该被释放（删除）
	 * @param mid 要删除的key
	 */
	private void unlock(String mid) {
		this.cache.remove(mid) ;
	}
	@Override
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		String mid = (String) token.getPrincipal() ; // 获取mid数据
		this.retry(mid); // 进行登录次数控制
		// 在父类之中提供有toString()方法可以自动将传递的字符数组密码变为字符串的密码
		Object tokenCredentials = PasswordUtils.encode(super.toString(token.getCredentials())) ;
		Object accountCredentials = super.getCredentials(info) ;	// 获取认证处理后的密码
		boolean flag = super.equals(tokenCredentials, accountCredentials) ; // 密码检测
		if (flag) {	// 登录成功
			this.unlock(mid); 
		}
		return flag ;
	}
	public void setExpire(String expire) {	// 设置失效时间
		this.expire = expire;
	}
	public void setMaxRetryCount(int maxRetryCount) { // 设置最多的尝试次数
		this.maxRetryCount = maxRetryCount;
	}
	public void setCacheManager(CacheManager cacheManager) {
		// 由于需要设置一个数据的有效时间，所以不能够使用Cache标准操作，要利用其子类处理
		this.cache = (RedisCache<Object,Object>) cacheManager.getCache("retryCount") ;
	}
}
