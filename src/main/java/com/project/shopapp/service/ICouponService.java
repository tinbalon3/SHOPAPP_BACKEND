package com.project.shopapp.service;

public interface ICouponService {
    double calculateCouponValue(String couponCode, double totalAmount);
    void activeCoupon(Long couponId, boolean active);
}
