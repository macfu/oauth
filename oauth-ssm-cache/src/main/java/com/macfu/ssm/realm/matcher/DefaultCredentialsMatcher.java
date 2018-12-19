package com.macfu.ssm.realm.matcher;

import com.macfu.ssm.utils.cache.RedisCache;
import com.macfu.util.enctype.PasswordUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.cache.CacheManager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: liming
 * @Date: 2018/12/17 16:43
 * @Description: 定义密码加密处理的密码匹配
 */
public class DefaultCredentialsMatcher extends SimpleCredentialsMatcher {
    // 在整个操作之中需要有一个Reis缓存配置项
    private RedisCache<Object, Object> cache;
    // 设置失效时间，默认时50s
    private String expire = "50";
    // 最多可以实现5次
    private int maxRetryCount = 5;

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        // 当前登录的用户
        String mid = (String) token.getPrincipal();
        // 进行登录次数尝试
        this.retry(mid);
        // 在父类中提供有toString()方法可以自动将传递的字符数组密码变为字符串密码
        Object tokenCredentials = PasswordUtils.encode(super.toString(token.getCredentials()));
        Object accountCredentials = super.getCredentials(info);
        boolean flag = super.equals(tokenCredentials, accountCredentials);
        // 登录成功
        if (flag) {
            this.unlock(mid);
        }
        return flag;
    }

    /**
     * 实现登录处理，如果要进行用户的登录技术，那么此时就应该将用户名作为数据中的key
     * @param mid 要保存在数据库的数据名(用户名)
     */
    public void retry(String mid) {
        // 使用AtomicInteger原子操作类是为了防止多个用户并发操作时候的不同步问题
        // 获取保存对象
        AtomicInteger num = (AtomicInteger) this.cache.get(mid);
        // 现在还没有相关的数据，用户没有登录或者登录成功
        if (num == null) {
            // 设置初始登录次数为1
            num = new AtomicInteger(1);
            // 保存信息
            this.cache.put(mid, num);
        }
        // 如果现在已经保存有登录次数了(你至少已经失败了一次)
        // 超过了最大尝试次数
        if (num.incrementAndGet() > this.maxRetryCount) {
            // 设置失效时间
            this.cache.putEx(mid, num, this.expire);
            throw new ExcessiveAttemptsException("用户" + mid + "密码尝试次数过多，请稍候重试！");
        } else {
            // 如果现在不够最大尝试次数
            // 保存当前尝试次数
            this.cache.put(mid, num.getAndIncrement());
        }
    }

    /**
     * 用户登录成功之后所有的数据应该被释放(删除)
     * @param mid 要删除的key
     */
    public void unlock(String mid) {
        this.cache.remove(mid);
    }

    /**
     * 设置失效时间
     * @param expire
     */
    public void setExpire(String expire) {
        this.expire = expire;
    }

    /**
     * 设置最多的尝试次数
     * @param maxRetryCount
     */
    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    /**
     * 由于需要一个数据的有效时间，所以不能够使用Cache标准操作，要利用其子类处理
     * @param cacheManager
     */
    public void setCacheManager(CacheManager cacheManager) {
        this.cache = (RedisCache<Object, Object>) cacheManager.getCache("retryCount");
    }
}