package com.project.shopapp.service;


import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.dto.OrderDetailHistoryDTO;
import com.project.shopapp.dto.OrderResponseDTO;
import com.project.shopapp.dto.PaymentObject;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.request.OrderUpdateRequest;
import com.project.shopapp.request.PurchaseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderService {

    Order createOrder(PurchaseRequest purchaseRequest) throws Exception;

    OrderResponseDTO getOrder(Long id) throws DataNotFoundException;

    Order updateOrder(Long id, OrderUpdateRequest orderUpdateRequest) throws DataNotFoundException;

    void deleteOrder(Long id) throws DataNotFoundException;
    List<Order> findByUserId(Long userID) throws DataNotFoundException;

    Order findByTransactionId(String transactionId) throws DataNotFoundException;

    Page<Order> getOrderByKeyword(Pageable pageable);
    Page<OrderDetailHistoryDTO> getOrderDetailHistory(String status,Long userID,Pageable pageable) throws DataNotFoundException;
    void updateStockAndOrder(Order order) throws Exception;
    void revertStockAndOrder(PaymentObject paymentObject) throws DataNotFoundException;
    Order findByOrderId(Long orderId) throws DataNotFoundException;
}
