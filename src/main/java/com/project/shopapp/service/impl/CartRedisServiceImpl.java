package com.project.shopapp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.controller.ProductController;
import com.project.shopapp.request.CartItemRequest;
import com.project.shopapp.service.ICartRedisService;
import io.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableCaching
@Service
public class CartRedisServiceImpl extends BaseRedisServiceImpl implements ICartRedisService {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private static final long TTL = 60 * 60; // 1 hour in seconds

    private String CART_KEY_PREFIX = "CART_";


    @Override
    public void addProductToCart(Long customerId, List<CartItemRequest> items) throws JsonProcessingException {
        String cartKey = CART_KEY_PREFIX + customerId;
        // Lấy bản đồ (Map) giỏ hàng hiện tại từ Redis
        Map<Long, CartItemRequest> existingCartItems = (Map<Long, CartItemRequest>) getMap(cartKey, Long.class, CartItemRequest.class);
        if (existingCartItems.isEmpty()) {
            existingCartItems = new HashMap<>();
        }

        // Thêm hoặc cập nhật các sản phẩm mới vào giỏ hàng
        for (CartItemRequest item : items) {
            if(item.getQuantity() == 0){
                existingCartItems.remove(item.getId());
            }else {
                existingCartItems.put(item.getId(), item);
            }
        }
        // Lưu giỏ hàng vào Redis
        saveMap(cartKey, existingCartItems);

    }


    @Override
    public List<CartItemRequest> getCartItems(Long customerId) throws JsonProcessingException {
        logger.info("(getCartItems): customerId = " + customerId);
        String cartKey = CART_KEY_PREFIX + customerId;

        // Lấy danh sách sản phẩm từ Redis
        Map<Long, CartItemRequest> existingCartItems = (Map<Long, CartItemRequest>) getMap(cartKey, Long.class, CartItemRequest.class);

        // Chuyển đổi từ Map values sang List
        return existingCartItems != null ? new ArrayList<>(existingCartItems.values()) : new ArrayList<>();
    }


    @Override
    public void clearCart(Long customerId) {
        logger.info("(clearProductToCart): customerId: " + customerId);
        String cartKey = CART_KEY_PREFIX + customerId;

        // Xóa toàn bộ giỏ hàng bằng cách xóa key trong Redis
        delete(cartKey);
        logger.info("Cart cleared for customer " + customerId);
    }

    @Override
    public void removeProductFromCart(Long customerId, Long productId) throws JsonProcessingException {
        String cartKey = CART_KEY_PREFIX + customerId;

        // Lấy giỏ hàng hiện tại từ Redis
        Map<Long, CartItemRequest> existingCartItems = (Map<Long, CartItemRequest>) getMap(cartKey, Long.class, CartItemRequest.class);

        // Tìm sản phẩm cần cập nhật trong giỏ hàng
        CartItemRequest cartItemRequest = existingCartItems.get(productId);
        if (cartItemRequest != null) {
            existingCartItems.remove(productId);
            saveMap(cartKey, existingCartItems);
        }


    }



}
