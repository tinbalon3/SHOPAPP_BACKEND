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


    @PostMapping("/add/{customerId}")
    public ResponseEntity<ResponseObject> addProductToCart(@RequestBody List<CartItemRequest> items, @PathVariable Long customerId) throws Exception{
        cartRedisService.addProductToCart(customerId,items);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật giỏ hàng thành công.")
                .build());

    }

    @GetMapping("/get/{customerId}")
    public  ResponseEntity<ResponseObject> getCart(@PathVariable Long customerId) throws Exception {
            List<CartItemRequest> cartItemRequests = cartRedisService.getCartItems(customerId);
            return ResponseEntity.ok(ResponseObject.builder()
                            .data(cartItemRequests)
                            .status(HttpStatus.OK.value())
                            .message("Lấy sản phẩm giỏ hàng thành công")
                    .build());
    }

    @DeleteMapping("/remove/{customerId}/{productId}")
    public ResponseEntity<ResponseObject> removeProductFromCart(@PathVariable Long customerId, @PathVariable Long productId) throws JsonProcessingException {
        cartRedisService.removeProductFromCart(customerId, productId);
        return ResponseEntity.ok(ResponseObject.builder()
                        .message("Xóa sản phẩm khỏi giỏ hàng thành công")
                        .status(HttpStatus.OK.value())
                .build());

    }


    @DeleteMapping("/clear/{customerId}")
    public ResponseEntity<ResponseObject> clearCart(@PathVariable Long customerId) {
        cartRedisService.clearCart(customerId);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Dọn sạch giỏ hàng thành công")
                .status(HttpStatus.OK.value())
                .build());

    }



}
