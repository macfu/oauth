package com.macfu.util.cache.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import cn.mldn.enterpriseauth.ssm.util.cache.RedisCache;
import cn.mldn.enterpriseauth.ssm.util.cache.abs.AbstractRedisCache;

public class RedisCacheManager implements CacheManager {
	// 建立一个负责管理所有缓存处理类的集合操作，要求保证线程安全
	private final ConcurrentMap<String, Cache<Object,Object>> CACHES = new ConcurrentHashMap<>() ;
	private Map<String,JedisConnectionFactory> connectionFactoryMap ;
	@Override
	public Cache<Object, Object> getCache(String name) throws CacheException {
		Cache<Object,Object> cache = CACHES.get(name) ; // 获得已经保存的缓存对象
		if (cache == null) {	// 没有缓存对象
			AbstractRedisCache<Object, Object> abstractCache = null ;
			if ("authentication".equals(name)) {	// 获得的是认证缓存
				abstractCache = new RedisCache<Object,Object>() ;
				abstractCache.setConnectionFactory(this.connectionFactoryMap.get("authentication"));
			} else if ("authorization".equals(name)) {	// 授权缓存
				abstractCache = new RedisCache<Object,Object>() ;
				abstractCache.setConnectionFactory(this.connectionFactoryMap.get("authorization"));
			} else if ("activeSessionCache".equals(name)) {
				abstractCache = new RedisCache<Object,Object>() ;
				abstractCache.setConnectionFactory(this.connectionFactoryMap.get("activeSessionCache"));
			} else if ("retryCount".equals(name)) {
				abstractCache = new RedisCache<Object,Object>() ;
				abstractCache.setConnectionFactory(this.connectionFactoryMap.get("retryCount"));
			} 
			cache = abstractCache ; // 获得缓存对象
		}
		return cache ; 
	}	
	// 此时需要管理多个Redis的连接控制，所以所有的连接通过外部配置
	public void setConnectionFactoryMap(Map<String, JedisConnectionFactory> connectionFactoryMap) {
		this.connectionFactoryMap = connectionFactoryMap;
	}
}
