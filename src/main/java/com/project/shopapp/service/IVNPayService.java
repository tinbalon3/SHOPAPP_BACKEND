package com.project.shopapp.service;

import com.project.shopapp.request.PurchaseRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface IVNPayService {
    String createOrder(Long orderId,float total, String reason, String urlReturn, String ipAddr) throws Exception;

    int orderReturn(HttpServletRequest request);
}
