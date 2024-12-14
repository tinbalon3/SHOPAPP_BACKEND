package com.project.shopapp.mapper;

import com.project.shopapp.dto.OrderDetailDTO;
import com.project.shopapp.dto.OrderHistoryDTO;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.request.OrderHistoryRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    OrderDetailMapper MAPPER = Mappers.getMapper(OrderDetailMapper.class);
   OrderDetailDTO mapToOrderDetailDTO(OrderDetail orderDetail);

   OrderHistoryRequest mapToOrderHistoryRequest(OrderHistoryDTO orderDetailHistoryDTO);

    List<OrderDetailDTO> mapToOrderDetailDTOList(List<OrderDetail> orderDetails);

    void updateOrderDetailFromDto(OrderDetailDTO orderDetailDTO, @MappingTarget OrderDetail orderDetail);
    @AfterMapping
    default void setAdditionalProperties(@MappingTarget OrderDetailDTO orderDetailDTO, OrderDetail orderDetail) {
        orderDetailDTO.setOrderId(orderDetail.getOrder().getId());
        orderDetailDTO.setProductId(orderDetail.getProduct().getId());
    }
}
