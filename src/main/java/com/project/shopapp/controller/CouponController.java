package com.project.shopapp.controller;

import com.project.shopapp.response.ResponseObject;
import com.project.shopapp.response.coupon.CouponCalculationResponse;
import com.project.shopapp.service.ICouponService;
import com.project.shopapp.service.impl.CouponServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final ICouponService couponService;

    @GetMapping("/calculate")
    public ResponseEntity<ResponseObject> calculateCouponValue(@RequestParam("couponCode") String couponCode,
                                                               @RequestParam("totalAmount") double totalAmount){
           double discount = couponService.calculateCouponValue(couponCode,totalAmount);
           return ResponseEntity.ok().body(
                   ResponseObject.builder()
                           .data(discount)
                           .status(HttpStatus.OK.value())
                           .message("Tính toán coupon giảm giá thành công")
                   .build());


    }
    @PutMapping("/active/{couponId}/{active}")
    public ResponseEntity<ResponseObject> setActive(@PathVariable Long couponId,@PathVariable boolean active){
        couponService.activeCoupon(couponId,active);
        String message = active == true ? "Mã giảm giá có hiệu lực." : "Mã giảm giá đã hết hiệu lực.";
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message(message)
                .status(HttpStatus.OK.value())
                .build());

    }
}
