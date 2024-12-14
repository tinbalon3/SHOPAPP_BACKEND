package com.project.shopapp.service;


import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.dto.OrderHistoryDTO;
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

    void createOrder(PurchaseRequest purchaseRequest) throws Exception;

    OrderResponseDTO getOrder(Long id) throws DataNotFoundException;

    void updateOrder(Long id, OrderUpdateRequest orderUpdateRequest) throws DataNotFoundException;

    void deleteOrder(Long id) throws DataNotFoundException;
    List<Order> findByUserId(Long userID) throws DataNotFoundException;

    Order saveOrder(Order order);
    Page<Order> getOrderByKeyword(Pageable pageable);
    Page<OrderHistoryDTO> getOrderHistory(String status, Long userID, Pageable pageable) throws DataNotFoundException;
    void updateStockAndOrder(Long orderId) throws Exception;
    void revertStockAndOrder(PaymentObject paymentObject) throws DataNotFoundException;
    Order findByOrderId(Long orderId) throws DataNotFoundException;
}
