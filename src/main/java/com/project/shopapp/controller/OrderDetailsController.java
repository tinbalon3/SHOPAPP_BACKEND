package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.OrderDetailDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.mapper.OrderDetailMapper;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.response.ResponseObject;
import com.project.shopapp.response.order.OrderDetailResponse;
import com.project.shopapp.response.order.OrderDetailsResponse;
import com.project.shopapp.service.IOrderDetailService;
import com.project.shopapp.untils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ResponseObject> createOrderDetails (@Valid @RequestBody OrderDetailDTO orderDetailDTO) throws DataNotFoundException {

            OrderDetail newOrderDetail = iOrderDetailService.createOrderDetail(orderDetailDTO);
            OrderDetailDTO orderDetailDTO1 = OrderDetailMapper.MAPPER.mapToOrderDetailDTO(newOrderDetail);

            return ResponseEntity.ok().body(ResponseObject.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.INSERT_ORDER_DETAIL_SUCCESSFULLY))
                            .data(orderDetailDTO1)
                            .status(HttpStatus.OK.value())
                    .build());



    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getOrderDetail(@Valid @PathVariable("id") Long id) throws DataNotFoundException {

            OrderDetailDTO newOrderDetail = iOrderDetailService.getOrderDetail(id);
            return ResponseEntity.ok(ResponseObject.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.FIND_ORDER_DETAIL_SUCCESSFULLY))
                            .data(newOrderDetail)
                            .status(HttpStatus.OK.value())
                    .build());
    }
    //lay ra danh sach cac order_details cua 1 order nao do
    @PreAuthorize("true")
    @GetMapping("/order/{id}")
    public ResponseEntity<ResponseObject> getOrderDetails(@Valid @PathVariable("id") Long id){
        List<OrderDetail> orderDetails = iOrderDetailService.findByOrderId(id);
        List<OrderDetailDTO> orderDetailDTOs = OrderDetailMapper.MAPPER.mapToOrderDetailDTOList(orderDetails);
        return ResponseEntity.ok(ResponseObject.builder()
                        .message(localizationUtils.getLocalizeMessage(MessageKeys.FIND_ORDER_DETAIL_SUCCESSFULLY))
                        .data(orderDetailDTOs)
                        .status(HttpStatus.OK.value())
                .build());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateOrderDetail(@Valid @PathVariable Long id,
                                               @RequestBody OrderDetailDTO orderDetailDTO) throws DataNotFoundException {


            OrderDetail orderDetail = iOrderDetailService.updateOrderDetail(id,orderDetailDTO);
            OrderDetailDTO orderDetailDTO1 = OrderDetailMapper.MAPPER.mapToOrderDetailDTO(orderDetail);
            return ResponseEntity.ok(ResponseObject.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.UPDATE_ORDER_DETAIL_SUCCESSFULLY,id))
                            .data(orderDetailDTO1)
                            .status(HttpStatus.OK.value())
                    .build());


    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteOrderDetail(@Valid @PathVariable Long id){
        iOrderDetailService.deleteOrderDetail(id);
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message(localizationUtils.getLocalizeMessage(MessageKeys.DELETE_ORDER_DETAIL_SUCCESSFULLY))
                .status(HttpStatus.OK.value())
                .build()
        );
    }
}
