package com.project.shopapp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.components.converters.OrderMessageConverter;
import com.project.shopapp.config.VnPayConfig;
import com.project.shopapp.controller.VNPayController;
import com.project.shopapp.dto.ItemPurchaseDTO;
import com.project.shopapp.enums.Status;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.models.OrderStatus;
import com.project.shopapp.models.Transactions;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.TransactionRepository;
import com.project.shopapp.request.PurchaseRequest;
import com.project.shopapp.service.IOrderDetailService;
import com.project.shopapp.service.ITransactionService;
import com.project.shopapp.service.IVNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service

public class VNPayServiceImpl extends BaseRedisServiceImpl  implements IVNPayService  {

    @Autowired
    private  ITransactionService transactionService;
    @Autowired
    private  OrderServiceImpl orderService;
    @Autowired
    private IOrderDetailService orderDetailService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(VNPayServiceImpl.class);



    @Transactional
    public String createOrder(float total, String orderInfor, String urlReturn, String ipAddr) throws Exception {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = generateTxnRef("vnp");
//        String vnp_IpAddr = "127.0.0.1";
        String vnp_TmnCode = VnPayConfig.vnp_TmnCode;
        String orderType = "order-type";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(10000 * 100));
        vnp_Params.put("vnp_BankCode","NCB");
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfor);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        urlReturn += VnPayConfig.vnp_ReturnURL;
        vnp_Params.put("vnp_ReturnUrl", urlReturn);
        vnp_Params.put("vnp_IpAddr", ipAddr);

        LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(currentTime);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        String vnp_ExpireDate = formatter.format(currentTime.plusMinutes(15));
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while(itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch(UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if(itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = VnPayConfig.vnp_PayUrl + "?" + queryUrl;

        return paymentUrl;
    }
    private String generateTxnRef(String prefix) {
        // Lấy thời gian hiện tại dưới dạng chuỗi
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = now.format(formatter);

        // Tạo một mã UUID để đảm bảo tính duy nhất
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 8); // lấy 8 ký tự đầu

        // Kết hợp tiền tố, thời gian và UUID để tạo mã giao dịch duy nhất
        return prefix + "_" + timestamp + "_" + uniqueId;
    }
    @Transactional
    public String processInfoOrder(HttpServletRequest request) throws  JsonProcessingException {
        int paymentStatus = orderReturn(request);


        Transactions transaction = transactionService.createTransaction(request);
        String hashKey = "orderPayment:user:" + transaction.getUser().getId();

        List<OrderDetail> orderDetailGetFromRedis = (List<OrderDetail>) getList(hashKey, "orderDetails",OrderDetail.class); // lấy giá trị từ Redis
        Order orderGetFromRedis = (Order) hashGetObject(hashKey,"order",Order.class); // lấy giá trị từ Redis
        Long userID = orderGetFromRedis.getUser().getId();

        if (paymentStatus == 1) {


            transaction.setStatus(Status.SUCCESS);
            orderGetFromRedis.setTransaction(transaction);
            orderGetFromRedis.setStatus(OrderStatus.PROCESSING);
            Order order = orderService.saveOrder(orderGetFromRedis);
            orderDetailGetFromRedis.forEach(orderDetail -> {
                orderDetail.setOrder(order);

            });
            orderDetailService.saveAllOrderDetail(orderDetailGetFromRedis);

            transaction.setOrder(order);
            transactionService.saveTransaction(transaction);

            kafkaTemplate.send("order-payments-success",order.getId());
            // Redirect về frontend khi thanh toán thành công
            String redirectUrl = VnPayConfig.vnp_RedictFE + "?" + "status=success";
            return redirectUrl;
        } else if (paymentStatus == 0) {

            kafkaTemplate.send("order-payments-fail",userID);
            // Redirect về trang thông báo thất bại trên FE
            logger.info("Thất bại khi thanh toán đơn hàng.");
            String redirectUrl = VnPayConfig.vnp_RedictFE + "?" +  "status=failed" ;
            return redirectUrl;
        } else {
            String redirectUrl = "http://localhost:4200/notfound";
            return redirectUrl;
        }
    }
    public int orderReturn(HttpServletRequest request) {
        Map fields = new HashMap();
        for(Enumeration params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            } catch(UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if(fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if(fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = VnPayConfig.hashAllFields(fields);
        if(signValue.equals(vnp_SecureHash)) {
            if("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }
}
