package com.project.shopapp.controller;

import com.project.shopapp.components.converters.OrderMessageConverter;
import com.project.shopapp.config.VnPayConfig;
import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.dto.PaymentObject;
import com.project.shopapp.enums.Status;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.mapper.OrderMapper;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderStatus;
import com.project.shopapp.models.Transactions;

import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.TransactionRepository;
import com.project.shopapp.request.PurchaseRequest;
import com.project.shopapp.response.purchase.PurchaseResponse;
import com.project.shopapp.service.IOrderService;
import com.project.shopapp.service.ITransactionService;
import com.project.shopapp.service.IUserService;
import com.project.shopapp.service.IVNPayService;
import com.project.shopapp.service.impl.BaseRedisServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController

@RequestMapping("${api.prefix}/vnpay")
public class VNPayController  {

    @Autowired
    private  IVNPayService vnPayService;

    @Autowired
    private  IOrderService orderService;


    @Value("${api.prefix}")
    private String api_prefix;


    @PostMapping(value = "/submitOrder", produces = "application/json;charset=UTF-8")
    public String submitOrder(@RequestBody PurchaseRequest purchaseRequest,
                              HttpServletRequest request) throws Exception {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + api_prefix;
        orderService.createOrder(purchaseRequest);
        String paymentUrl = vnPayService.createOrder(purchaseRequest.getTotalMoney(), purchaseRequest.getReason(), baseUrl, request.getRemoteAddr());
        return paymentUrl;
    }


    @GetMapping("/getPaymentInfo")
    public void getPaymentInfo(HttpServletRequest request, HttpServletResponse response) throws IOException, DataNotFoundException {
        String redirectUrl = vnPayService.processInfoOrder(request);
        response.sendRedirect(redirectUrl);
    }

}
