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
public class VNPayController extends BaseRedisServiceImpl {

    @Autowired
    private  IVNPayService vnPayService;
    @Autowired
    private  TransactionRepository transactionRepository;
    @Autowired
    private  ITransactionService iTransactionService;
    @Autowired
    private   IOrderService iOrderService;
    @Autowired
    private  OrderRepository orderRepository;
    @Autowired
    private  KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${api.prefix}")
    private String api_prefix;

    private static final Logger logger = LoggerFactory.getLogger(VNPayController.class);

    public VNPayController(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    @PostMapping(value = "/submitOrder", produces = "application/json;charset=UTF-8")
    public String submitOrder(@RequestBody PurchaseRequest purchaseRequest,
                              HttpServletRequest request) throws Exception {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + api_prefix;
        Order order = iOrderService.createOrder(purchaseRequest);
        String paymentUrl = vnPayService.createOrder(order.getId(),purchaseRequest.getTotalMoney(), purchaseRequest.getReason(), baseUrl, request.getRemoteAddr());
        return paymentUrl;
    }


    @GetMapping("/getPaymentInfo")
    public void getPaymentInfo(HttpServletRequest request, HttpServletResponse response) throws IOException, DataNotFoundException {

            int paymentStatus = vnPayService.orderReturn(request);
            Transactions transaction = iTransactionService.createTransaction(request);
            String billNo = transaction.getBillNo();
            Optional<Order> order = orderRepository.findById(Long.parseLong(billNo));
            Long userID = order.get().getUser().getId();
            Long orderID = order.get().getId();
            String orderKeyUser = "order:user:" + userID;
            setLong(orderKeyUser,orderID);
            if (paymentStatus == 1) {
                transaction.setStatus(Status.SUCCESS);
                transaction.setOrder(order.get());
                transactionRepository.save(transaction);
                order.get().setTransaction(transaction);
                orderRepository.save(order.get());
                String orderTrackingNumber = order.get().getTrackingNumber();
                kafkaTemplate.send("order-payments-success",String.valueOf(order.get().getId()),userID);
                // Redirect về frontend khi thanh toán thành công
                String redirectUrl = VnPayConfig.vnp_RedictFE + "/" + orderTrackingNumber ;
                response.sendRedirect(redirectUrl);
            } else if (paymentStatus == 0) {
                //
                transaction.setStatus(Status.FAIL);
                transactionRepository.save(transaction);
                kafkaTemplate.setMessageConverter(new OrderMessageConverter());
                kafkaTemplate.send("order-payments-fail",String.valueOf(order.get().getId()),userID);
                // Redirect về trang thông báo thất bại trên FE
                logger.info("Thất bại khi thanh toán đơn hàng.");
                response.sendRedirect("http://localhost:4200");
            } else {
                response.getWriter().write("Lỗi !!! Mã Secure Hash không hợp lệ.");
            }
    }

}
