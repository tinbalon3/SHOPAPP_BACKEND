package com.project.shopapp.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.request.PurchaseRequest;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;
import java.util.Map;
import java.util.Set;
public interface IBaseRedisService {
    void setString(String key, String value);
    void setInt(String key, int value);

    void setLong(String key, Long value);
    void setTimeToLive(String key, long timeoutInDays);

    List<?> getList(String key, String field, Class<?> clazz) throws JsonProcessingException;

    <T> List<T> getListAllValue(String key, Class<T> valueClass) throws JsonProcessingException;
    void hashSet(String key, String field, Object value) throws JsonProcessingException;



    boolean hashExists(String key, String field);

    Object get(String key);
    Long increment(String key);
    Long decrement(String key);

    Long incrementBy(String key,int value);
    Long decrementBy(String key,int value);
    void setExpired(String key, long timeInSeconds);


    public Map<String, Object> getField(String key);



     void hashSet(String key, String field, Integer value);


    <T> T hashGetObject(String key, String field, Class<T> clazz);

    List<Object> hashGetByFieldPrefix(String key, String filedPrefix);

    Set<String> getFieldPrefixes(String key);

    void delete(String key);

    void delete(String key, String field);

    void delete(String key, List<String> fields);

     void saveMap(String key, Map<?, ?> map) throws JsonProcessingException;
      Map<?, ?> getMap(String key,  Class<?> keyClass, Class<?> valueClass) throws JsonProcessingException;
    public void saveObject(String key, Object object) throws JsonProcessingException;
    public <T> T getObject(String key, Class<T> clazz) throws JsonProcessingException;
     void saveList(String key, List<?> list) throws JsonProcessingException;
//     List<?> getList(String key,  Class<?> clazz) throws JsonProcessingException;

}
