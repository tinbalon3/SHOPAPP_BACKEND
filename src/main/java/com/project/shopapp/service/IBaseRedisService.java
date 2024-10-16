package com.project.shopapp.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;
public interface IBaseRedisService {
    void set(String key, String value);

    void setTimeToLive(String key, long timeoutInDays);

    void hashSet(String key, String field, Object value);

    boolean hashExists(String key, String field);

    Object get(String key);

    public Map<String, Object> getField(String key);

    Object hashGet(String key, String field);

     void hashSet(String key, String field, Integer value);


    List<Object> hashGetByFieldPrefix(String key, String filedPrefix);

    Set<String> getFieldPrefixes(String key);

    void delete(String key);

    void delete(String key, String field);

    void delete(String key, List<String> fields);

     void saveMap(String key, Map<?, ?> map) throws JsonProcessingException;
      Map<?, ?> getMap(String key,  Class<?> keyClass, Class<?> valueClass) throws JsonProcessingException;

     void saveList(String key, List<?> list) throws JsonProcessingException;
     List<?> getList(String key,  Class<?> clazz) throws JsonProcessingException;

}
