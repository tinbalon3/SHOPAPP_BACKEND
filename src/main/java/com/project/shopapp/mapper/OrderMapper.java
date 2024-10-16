package com.project.shopapp.mapper;

import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.dto.OrderResponseDTO;
import com.project.shopapp.models.Order;
import com.project.shopapp.request.PurchaseRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderMapper {
    OrderMapper MAPPER = Mappers.getMapper(OrderMapper.class);

    OrderDTO mapToOrderDTO(Order order);

    Order mapToOrder(PurchaseRequest purchaseRequest);
    OrderResponseDTO mapToOrderResponseDTO(Order order);

    void updateOrderFromDto(PurchaseRequest purchaseRequest, @MappingTarget Order order);
}
