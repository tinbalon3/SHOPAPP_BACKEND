package com.project.shopapp.service;

import jakarta.servlet.http.HttpServletRequest;

public interface IVNPayService {
    String createOrder(float total, String reason, String urlReturn, String ipAddr);

    int orderReturn(HttpServletRequest request);
}
