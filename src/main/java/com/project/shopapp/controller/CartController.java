package com.project.shopapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.request.CartItemRequest;
import com.project.shopapp.response.ResponseObject;
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
    public ResponseEntity<?> addProductToCart(@RequestBody List<CartItemRequest> items, @PathVariable Long customerId) throws Exception{

            cartRedisService.addProductToCart(customerId,items);
            return ResponseEntity.ok().build();

    }

    @GetMapping("/get/{customerId}")
    public  ResponseEntity<ResponseObject> getCart(@PathVariable Long customerId) throws Exception {
            return ResponseEntity.ok(ResponseObject.builder()
                            .data(cartRedisService.getCartItems(customerId))
                            .status(HttpStatus.OK)
                            .message("Lấy sản phẩm giỏ hàng thành công")
                    .build());
    }

    @DeleteMapping("/remove/{customerId}/{productId}")
    public ResponseEntity<ResponseObject> removeProductFromCart(@PathVariable Long customerId, @PathVariable Long productId) {
        cartRedisService.removeProductFromCart(customerId, productId);
        return ResponseEntity.ok(ResponseObject.builder()
                        .message("Xóa sản phẩm khỏi giỏ hàng thành công")
                        .status(HttpStatus.OK)

                .build());

    }


    @DeleteMapping("/clear/{customerId}")
    public ResponseEntity<?> clearCart(@PathVariable Long customerId) {
        cartRedisService.clearCart(customerId);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping ("/update/{customerId}/{productId}/{quantity}")
    public ResponseEntity<ResponseObject> updateCart(@PathVariable Long customerId,@PathVariable Long productId, @PathVariable Integer quantity) throws JsonProcessingException {
        CartItemRequest cartItemRequest = cartRedisService.updateItems(customerId,productId,quantity);
        return ResponseEntity.ok(ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Cập nhật giỏ hàng thành công")
                        .data(cartItemRequest)
                .build());
    }
}
