package com.project.shopapp.service;


import com.project.shopapp.dto.OrderDetailHistoryDTO;
import com.project.shopapp.dto.OrderResponseDTO;
import com.project.shopapp.exception.DataNotFoundException;
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

    void deleteOrder(Long id);
    List<Order> findByUserId(Long userID);

    Page<Order> getOrderByKeyword(Pageable pageable);
    Page<OrderDetailHistoryDTO> getOrderDetailHistory(String status,Long userID,Pageable pageable) throws DataNotFoundException;
}
