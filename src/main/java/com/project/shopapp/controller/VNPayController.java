package com.project.shopapp.controller;

import com.project.shopapp.VNPAYMODEL.CreateVNPAYModel;
import com.project.shopapp.config.VnPayConfig;
import com.project.shopapp.enums.Status;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.Transactions;

import com.project.shopapp.repositories.TransactionRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.request.PurchaseRequest;
import com.project.shopapp.response.purchase.PurchaseResponse;
import com.project.shopapp.service.IOrderService;
import com.project.shopapp.service.ITransactionService;
import com.project.shopapp.service.IVNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/vnpay")
public class VNPayController {


    private final IVNPayService vnPayService;

    private final TransactionRepository transactionRepository;
    private final ITransactionService iTransactionService;

    private final  IOrderService iOrderService;
    @Value("${api.prefix}")
    private String api_prefix;
    private PurchaseRequest purchase;
    private static final Logger logger = LoggerFactory.getLogger(VNPayController.class);

    @PostMapping(value = "/submitOrder", produces = "application/json;charset=UTF-8")
    public String submitOrder(@RequestBody PurchaseRequest createVNPAYModel,
                              HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + api_prefix;
        logger.info(createVNPAYModel.toString());
        this.purchase = createVNPAYModel;
        return vnPayService.createOrder(createVNPAYModel.getTotalMoney(), createVNPAYModel.getReason(), baseUrl, request.getRemoteAddr());
    }

    private String createOrder(PurchaseRequest purchaseRequest){
        try{

            Order order = iOrderService.createOrder(purchaseRequest);
            PurchaseResponse purchaseResponse = new PurchaseResponse(order.getTrackingNumber());
            logger.info(purchaseResponse.getOrderTrackingNumber());
            return purchaseResponse.getOrderTrackingNumber();
        }catch (Exception e){
            throw new RuntimeException("Error occurred while creating the order. Please try again later.");
        }
    }
    @GetMapping("/getPaymentInfo")
    public void getPaymentInfo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int paymentStatus = vnPayService.orderReturn(request);
        try {
            Transactions transaction = iTransactionService.createTransaction(request);
            if (paymentStatus == 1) {
                transaction.setStatus(Status.SUCCESS);
                transactionRepository.save(transaction);
                // Redirect về frontend khi thanh toán thành công
                String orderTracking = this.createOrder(purchase);
                String redirectUrl = VnPayConfig.vnp_RedictFE +"/"+orderTracking;
                response.sendRedirect(redirectUrl);

            } else if (paymentStatus == 0) {
                transaction.setStatus(Status.FAIL);
                transactionRepository.save(transaction);
                // Redirect về trang thông báo thất bại trên FE
                response.getWriter().write("That bai roi");
//            response.sendRedirect("http://localhost:4200");
            } else {
                response.getWriter().write("Lỗi !!! Mã Secure Hash không hợp lệ.");
            }
        } catch (Exception e) {
            throw new RuntimeException("khong tao transaction duoc");
        }


    }

}
