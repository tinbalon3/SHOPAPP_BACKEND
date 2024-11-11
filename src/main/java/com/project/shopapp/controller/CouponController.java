package com.project.shopapp.controller;

import com.project.shopapp.response.ResponseObject;
import com.project.shopapp.response.coupon.CouponCalculationResponse;
import com.project.shopapp.service.ICouponService;
import com.project.shopapp.service.impl.CouponServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final ICouponService iCouponService;

    @GetMapping("/calculate")
    public ResponseEntity<ResponseObject> calculateCouponValue(@RequestParam("couponCode") String couponCode,
                                                               @RequestParam("totalAmount") double totalAmount){

           double finalAmount = iCouponService.calculateCouponValue(couponCode,totalAmount);
           return ResponseEntity.ok().body(
                   ResponseObject.builder()
                           .data(finalAmount)
                           .status(HttpStatus.OK)
                           .message("Tính toán coupon giảm giá thành công")
                   .build());


    }
}
