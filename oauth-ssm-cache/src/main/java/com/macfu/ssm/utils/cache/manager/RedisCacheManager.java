package com.macfu.ssm.utils.cache.manager;

import com.macfu.ssm.utils.cache.RedisCache;
import com.macfu.ssm.utils.cache.abs.AbstractRedisCache;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: liming
 * @Date: 2018/12/18 17:08
 * @Description:
 */
public class RedisCacheManager implements CacheManager {
    // 建议一个负责管理所有缓存处理类的集合操作，要求保证线程安全
    private final ConcurrentHashMap<String, Cache<Object, Object>> CACHES = new ConcurrentHashMap<>();
    private Map<String, JedisConnectionFactory> connectionFactoryMap;

    @Override
    public Cache<java.lang.Object, java.lang.Object> getCache(String name) throws CacheException {
        Cache<Object, Object> cache = CACHES.get(name);
        // 还没有创建该缓存管理的对象就需要进行对象的创建处理
        if (cache == null) {
            AbstractRedisCache<Object, Object> abstractRedisCache = null;
            // 要获取的是认证缓存
            if ("authenticationCache".equals(name)) {
                abstractRedisCache = new RedisCache<Object, Object>();
                abstractRedisCache.setConnectionFactory(this.connectionFactoryMap.get("authenticationCache"));
                // 获取的是授权缓存
            } else if ("authorizationCache".equals(name)) {
                abstractRedisCache = new RedisCache<>();
                abstractRedisCache.setConnectionFactory(this.connectionFactoryMap.get("authorizationCache"));
                // 取得session缓存activeSessionCache
            } else if ("activeSessionCache".equals(name)) {
                abstractRedisCache = new RedisCache<Object, Object>();
                abstractRedisCache.setConnectionFactory(this.connectionFactoryMap.get("activeSessionCache"));
            }
            cache = abstractRedisCache;
            // 防止重复取出
            CACHES.put(name, cache);
        }
        return cache;
    }

    public void setConnectionFactoryMap(Map<String, JedisConnectionFactory> connectionFactoryMap) {
        this.connectionFactoryMap = connectionFactoryMap;
    }
}
