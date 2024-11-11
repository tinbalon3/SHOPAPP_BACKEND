package com.project.shopapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.request.CartItemRequest;

import java.util.List;
import java.util.Map;

public interface ICartRedisService extends IBaseRedisService {
    void addProductToCart(Long customerId, List<CartItemRequest> list) throws JsonProcessingException;
    List<CartItemRequest> getCartItems(Long customerId) throws JsonProcessingException;
    void removeProductFromCart(Long customerId, Long productId);
    void clearCart(Long customerId);
    CartItemRequest updateItems(Long customerId, Long productId, int quantity) throws JsonProcessingException;
}
