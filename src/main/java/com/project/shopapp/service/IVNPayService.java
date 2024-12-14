package com.project.shopapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.request.PurchaseRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface IVNPayService {
    String createOrder(float total, String reason, String urlReturn, String ipAddr) throws Exception;

    int orderReturn(HttpServletRequest request);

    String processInfoOrder(HttpServletRequest request) throws DataNotFoundException, JsonProcessingException;
}
