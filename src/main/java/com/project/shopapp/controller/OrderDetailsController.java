package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.OrderDetailDTO;
import com.project.shopapp.mapper.OrderDetailMapper;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.response.order.OrderDetailResponse;
import com.project.shopapp.response.order.OrderDetailsResponse;
import com.project.shopapp.service.IOrderDetailService;
import com.project.shopapp.untils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order_details")
@RequiredArgsConstructor
public class OrderDetailsController {

    private final IOrderDetailService iOrderDetailService;
    private final LocalizationUtils localizationUtils;
    //Them moi 1 order details
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("")
    public ResponseEntity<?> createOrderDetails(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO
            )  {
        try{
            OrderDetail newOrderDetail = iOrderDetailService.createOrderDetail(orderDetailDTO);
            OrderDetailDTO orderDetailDTO1 = OrderDetailMapper.MAPPER.mapToOrderDetailDTO(newOrderDetail);

            return ResponseEntity.ok().body(OrderDetailResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.INSERT_ORDER_DETAIL_SUCCESSFULLY))
                            .orderDetail(orderDetailDTO1)
                    .build());
        }catch (Exception e){
            return ResponseEntity.ok().body(OrderDetailResponse.builder()
                    .message(e.getMessage())
                    .orderDetail(null)
                    .build());
        }


    }
    @PreAuthorize("true")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(
            @Valid @PathVariable("id") Long id
    ){
        try{
            OrderDetail newOrderDetail = iOrderDetailService.getOrderDetail(id);
            OrderDetailDTO orderDetailDTO = OrderDetailMapper.MAPPER.mapToOrderDetailDTO(newOrderDetail);
            orderDetailDTO.setOrderId(newOrderDetail.getOrder().getId());
            orderDetailDTO.setProductId(newOrderDetail.getProduct().getId());
            return ResponseEntity.ok(OrderDetailResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.FIND_ORDER_DETAIL_SUCCESSFULLY))
                            .orderDetail(orderDetailDTO)
                            .orderDetail(orderDetailDTO)
                    .build());
        }catch (Exception e){
            return ResponseEntity.ok(OrderDetailResponse.builder()
                    .message(e.getMessage())
                    .orderDetail(null)
                    .build());
        }

    }
    //lay ra danh sach cac order_details cua 1 order nao do
    @PreAuthorize("true")
    @GetMapping("/order/{id}")
    public ResponseEntity<?> getOrderDetails(
            @Valid @PathVariable("id") Long id
    ){
        List<OrderDetail> orderDetails = iOrderDetailService.findByOrderId(id);
        List<OrderDetailDTO> orderDetailDTOs = OrderDetailMapper.MAPPER.mapToOrderDetailDTOList(orderDetails);
        return ResponseEntity.ok(OrderDetailsResponse.builder()
                        .message(localizationUtils.getLocalizeMessage(MessageKeys.FIND_ORDER_DETAIL_SUCCESSFULLY))
                        .orderDetails(orderDetailDTOs)
                .build());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail(@Valid @PathVariable Long id,
                                               @RequestBody OrderDetailDTO orderDetailDTO) {

        try{
            OrderDetail orderDetail = iOrderDetailService.updateOrderDetail(id,orderDetailDTO);
            OrderDetailDTO orderDetailDTO1 = OrderDetailMapper.MAPPER.mapToOrderDetailDTO(orderDetail);
            return ResponseEntity.ok(OrderDetailResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.UPDATE_ORDER_DETAIL_SUCCESSFULLY,id))
                            .orderDetail(orderDetailDTO1)
                    .build());
        }catch (Exception e){
            return ResponseEntity.ok(OrderDetailResponse.builder()
                    .message(e.getMessage())
                    .orderDetail(null)
                    .build());
        }

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrderDetail(@Valid @PathVariable Long id){
        iOrderDetailService.deleteOrderDetail(id);
        return ResponseEntity.ok().body(OrderDetailResponse.builder()
                .message(localizationUtils.getLocalizeMessage(MessageKeys.DELETE_ORDER_DETAIL_SUCCESSFULLY))
                .orderDetail(null)
                .build()
        );
    }
}
