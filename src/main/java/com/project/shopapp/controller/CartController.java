package com.project.shopapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.request.CartItemRequest;
import com.project.shopapp.service.impl.CartRedisServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartRedisServiceImpl cartRedisService;
    private final LocalizationUtils localizationUtils;


    @PostMapping("/add/{customerId}")
    public ResponseEntity<?> addProductToCart(@RequestBody List<CartItemRequest> items, @PathVariable Long customerId)  {
        try {
            cartRedisService.addProductToCart(customerId,items);
            return ResponseEntity.ok().build();
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }


    @GetMapping("/get/{customerId}")
    public  ResponseEntity<?> getCart(@PathVariable Long customerId)  {
        try {
            return ResponseEntity.ok(cartRedisService.getCartItems(customerId));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/remove/{customerId}/{productId}")
    public ResponseEntity<?> removeProductFromCart(@PathVariable Long customerId, @PathVariable Long productId) {
        cartRedisService.removeProductFromCart(customerId, productId);
        return ResponseEntity.ok("Products remove to cart successfully");
    }


    @DeleteMapping("/clear/{customerId}")
    public ResponseEntity<?> clearCart(@PathVariable Long customerId) {
        cartRedisService.clearCart(customerId);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping ("/update/{customerId}/{productId}/{quantity}")
    public ResponseEntity<?> updateCart(@PathVariable Long customerId,@PathVariable Long productId, @PathVariable Integer quantity) throws JsonProcessingException {
        CartItemRequest cartItemRequest = cartRedisService.updateItems(customerId,productId,quantity);
        return ResponseEntity.ok(cartItemRequest);
    }
}
