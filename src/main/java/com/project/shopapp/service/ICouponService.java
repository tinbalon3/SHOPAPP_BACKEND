package com.project.shopapp.service;

public interface ICouponService {
    double calculateCouponValue(String couponCode, double totalAmount);
}
