package com.project.shopapp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.service.IBaseRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
@Service

public class BaseRedisServiceImpl implements IBaseRedisService {
    protected final RedisTemplate<String, Object> redisTemplate;
    protected final HashOperations<String, String, Object> hashOperations;


    @Autowired
    public BaseRedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public void saveList(String key, List<?> list) throws JsonProcessingException {
        ObjectMapper redisObjectMapper = new ObjectMapper();
        String json = redisObjectMapper.writeValueAsString(list);
        this.set(key,json);
    }
    @Override
    public List<?> getList(String key, Class<?> clazz) throws JsonProcessingException {
        String json = (String) redisTemplate.opsForValue().get(key);
        if (json != null) {
            ObjectMapper redisObjectMapper = new ObjectMapper();
            return redisObjectMapper.readValue(json, redisObjectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        }
        return  Collections.emptyList();
    }

    @Override
    public void saveMap(String key, Map<?, ?> map) throws JsonProcessingException {
        ObjectMapper redisObjectMapper = new ObjectMapper();
        String json = redisObjectMapper.writeValueAsString(map);
        this.set(key, json);
    }


    @Override
    public Map<?, ?> getMap(String key, Class<?> keyClass, Class<?> valueClass) throws JsonProcessingException {
        String json = (String) redisTemplate.opsForValue().get(key);
        if (json != null) {
            ObjectMapper redisObjectMapper = new ObjectMapper();
            return redisObjectMapper.readValue(json, redisObjectMapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
        }
        return Collections.emptyMap();
    }

    @Override
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setTimeToLive(String key, long timeoutInDays) {
        redisTemplate.expire(key, timeoutInDays, TimeUnit.DAYS);
    }


    @Override
    public void hashSet(String key, String field, Integer value) {
        hashOperations.put(key, field, value);
    }
    @Override
    public boolean hashExists(String key, String field) {

        return hashOperations.hasKey(key, field);
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Map<String, Object> getField(String key) {
        return hashOperations.entries(key);
    }
    @Override
    public void hashSet(String key, String field, Object value) {
        hashOperations.put(key, field, value);
    }
    @Override
    public Object hashGet(String key, String field) {
        return  hashOperations.get(key, field);
    }

    @Override
    public List<Object> hashGetByFieldPrefix(String key, String filedPrefix) {
        List<Object> objects = new ArrayList<>();

        Map<String, Object> hashEntries = hashOperations.entries(key);

        for (Map.Entry<String, Object> entry : hashEntries.entrySet()) {
            if (entry.getKey().startsWith(filedPrefix)) {
                objects.add(entry.getValue());
            }
        }
        return objects;
    }

    @Override
    public Set<String> getFieldPrefixes(String key) {
        return hashOperations.entries(key).keySet();
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void delete(String key, String field) {
        hashOperations.delete(key, field);
    }

    @Override
    public void delete(String key, List<String> fields) {
        for (String field : fields) {
            hashOperations.delete(key, field);
        }
    }
}
