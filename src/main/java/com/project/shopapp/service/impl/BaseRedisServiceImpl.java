package com.project.shopapp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.service.IBaseRedisService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
@Service

public class BaseRedisServiceImpl implements IBaseRedisService {
    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    @Autowired
    protected  ObjectMapper redisObjectMapper;

    protected  HashOperations<String, String, Object> hashOperations;

    @PostConstruct
    public void init() {
        // Khởi tạo HashOperations sau khi các đối tượng được inject
        this.hashOperations = redisTemplate.opsForHash();
    }


    @Override
    public void saveList(String key, List<?> list) throws JsonProcessingException {
        ObjectMapper redisObjectMapper = new ObjectMapper();
        String json = redisObjectMapper.writeValueAsString(list);
        this.setString(key,json);
    }
    @Override
    public void saveMap(String key, Map<?, ?> map) throws JsonProcessingException {
        ObjectMapper redisObjectMapper = new ObjectMapper();
        String json = redisObjectMapper.writeValueAsString(map);
        this.setString(key, json);
    }
    @Override
    public void saveObject(String key, Object aClass) throws JsonProcessingException {
        // Tạo một ObjectMapper để chuyển đổi đối tượng thành JSON
//        ObjectMapper redisObjectMapper = new ObjectMapper();
        String json = redisObjectMapper.writeValueAsString(aClass);

        // Lưu vào Redis bằng phương thức set
        this.setString(key, json);
    }
    @Override
    public <T> T getObject(String key, Class<T> clazz) throws JsonProcessingException {
        // Lấy chuỗi JSON từ Redis
        String json = (String) redisTemplate.opsForValue().get(key);

        if (json == null) {
            return null;  // Trường hợp không có giá trị trong Redis
        }

        // Chuyển đổi chuỗi JSON thành đối tượng

        return redisObjectMapper.readValue(json, clazz);
    }

    @Override
    public List<?> getList(String key, String field, Class<?> clazz) throws JsonProcessingException {
        String jsonString = (String) hashOperations.get(key, field);

        if (jsonString != null) {

            return redisObjectMapper.readValue(jsonString, redisObjectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        }
        return  Collections.emptyList();
    }
    @Override
    public <T> List<T> getListAllValue(String key, Class<T> valueClass) throws JsonProcessingException {
        // Lấy dữ liệu từ Redis dưới dạng danh sách các đối tượng (Object)
        List<Object> redisList = redisTemplate.opsForList().range(key, 0, -1);

        // Kiểm tra nếu danh sách không null
        if (redisList != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            List<T> resultList = new ArrayList<>();

            for (Object obj : redisList) {
                // Chuyển đổi mỗi đối tượng trong danh sách thành kiểu T
                String json = objectMapper.writeValueAsString(obj);  // Chuyển Object thành String JSON
                T entity = objectMapper.readValue(json, valueClass); // Chuyển String JSON thành kiểu T
                resultList.add(entity);  // Thêm vào danh sách kết quả
            }

            return resultList;
        }

        return Collections.emptyList(); // Trả về danh sách rỗng nếu không có dữ liệu
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
    public void setString(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
    @Override
    public void setInt(String key, int value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setLong(String key, Long value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setTimeToLive(String key, long timeoutInSeconds) {
        redisTemplate.expire(key, timeoutInSeconds, TimeUnit.SECONDS);
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
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
    @Override
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }
    @Override
    public Long incrementBy(String key,int value) {
        return redisTemplate.opsForValue().increment(key,value);
    }
    @Override
    public Long decrementBy(String key,int value) {
        return redisTemplate.opsForValue().decrement(key,value);
    }
    @Override
    public void setExpired(String key, long timeInSeconds) {
        redisTemplate.expire(key, Duration.ofSeconds(timeInSeconds));
    }

    @Override
    public Map<String, Object> getField(String key) {
        return hashOperations.entries(key);
    }




    @Override
    public void hashSet(String key, String field, Object value) throws JsonProcessingException {
        String jsonString = redisObjectMapper.writeValueAsString(value);
        hashOperations.put(key, field, jsonString);

    }
    @Override
    public <T> T hashGetObject(String key, String field, Class<T> clazz) {
        String jsonString = (String) hashOperations.get(key, field);
        if (jsonString != null) {
            try {
                return redisObjectMapper.readValue(jsonString, clazz);
            } catch (JsonProcessingException e) {

                // Handle the error, e.g., return a default value or throw an exception
                return null; // Or throw a custom exception
            }
        } else {
            return null;
        }
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
