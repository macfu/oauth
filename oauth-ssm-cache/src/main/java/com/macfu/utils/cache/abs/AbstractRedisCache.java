package com.macfu.utils.cache.abs;

import com.google.common.collect.Sets;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.SerializationUtils;

import java.util.Collection;
import java.util.Set;

/**
 * @Author: liming
 * @Date: 2018/12/17 14:57
 * @Description: Redis操作工具类
 */
public abstract class AbstractRedisCache<K, V> implements Cache<K, V> {
    private JedisConnectionFactory connectionFactory;

    public void setConnectionFactory(JedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * 由于RedisConnectionFactory的所有操作都是以字节数组的形式出现的，所以直接设置一个工具方法转换
     *
     * @param obj 要转换的字节数组
     * @return 对象的字节数组
     */
    protected byte[] objectToArray(Object obj) {
        return SerializationUtils.serialize(obj);
    }

    /**
     * 将字节数组重新变为Object对象
     *
     * @param data 要转变的字节数组
     * @return 目标处理对象
     */
    protected Object byteArrayToObject(byte[] data) {
        return SerializationUtils.deserialize(data);
    }

    @Override
    public V get(K k) throws CacheException {
        V obj = null;
        RedisConnection connection = this.connectionFactory.getConnection();
        try {
            obj = (V) this.byteArrayToObject(connection.get(this.objectToArray(k)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.close();
        return obj;
    }

    @Override
    public V put(K k, V v) throws CacheException {
        RedisConnection connection = this.connectionFactory.getConnection();
        try {
            connection.set(this.objectToArray(k), this.objectToArray(v));
        } catch (Exception e) {
        }
        connection.close();
        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        V obj = null;
        RedisConnection connection = this.connectionFactory.getConnection();
        try {
            obj = (V) this.byteArrayToObject(connection.get(this.objectToArray(k)));
            connection.del(this.objectToArray(k));
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.close();
        return obj;
    }

    @Override
    public void clear() throws CacheException {
        RedisConnection connection = this.connectionFactory.getConnection();
        try {
            // 删除数据库中的所有数据
            connection.flushDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.close();
    }

    @Override
    public int size() {
        int size = 0;
        RedisConnection connection = this.connectionFactory.getConnection();
        try {
            Set<byte[]> keys = connection.keys(this.objectToArray("*"));
            size = keys.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.close();
        return size;
    }

    @Override
    public Set<K> keys() {
        // 返回全部的keys
        Set<K> allKeys = Sets.newHashSet();
        RedisConnection connection = this.connectionFactory.getConnection();
        try {
            Set<byte[]> keys = connection.keys(this.objectToArray("*"));
            for (byte[] key : keys) {
                allKeys.add((K) this.byteArrayToObject(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.close();
        return allKeys;
    }

    @Override
    public Collection<V> values() {
        // 返回所有的value信息
        Set<V> allValues = Sets.newHashSet();
        RedisConnection connection = this.connectionFactory.getConnection();
        try {
            Set<byte[]> keys = connection.keys(this.objectToArray("*"));
            for (byte[] key : keys) {
                allValues.add((V) this.byteArrayToObject(connection.get(key)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.close();
        return allValues;
    }

    /**
     * 设置一个Redis的数据操作，其本身需要一个失效时间
     * @param key 要设置的key
     * @param value 要设置的value
     * @param expire 失效时间
     * @return 返回Value
     * @throws CacheException 缓存异常
     */
    public V putEx(K key, V value,Long expire) throws CacheException {
        RedisConnection connection = this.connectionFactory.getConnection() ;
        try {
            connection.setEx(this.objectToArray(key), expire, this.objectToArray(value)) ;
        } catch (Exception e) {}
        connection.close(); 	// 将连接交回到连接池之中
        return value;
    }
    /**
     * 设置一个Redis的数据操作，其本身需要一个失效时间
     * @param key 要设置的key
     * @param value 要设置的value
     * @param expire 失效时间
     * @return 返回Value
     * @throws CacheException 缓存异常
     */
    public V putEx(K key, V value,String expire) throws CacheException {
        return this.putEx(key, value, Long.parseLong(expire)) ;
    }
}
