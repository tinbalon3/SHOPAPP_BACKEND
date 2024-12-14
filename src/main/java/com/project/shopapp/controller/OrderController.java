package com.project.shopapp.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.components.LocalizationUtils;

import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.dto.OrderHistoryDTO;
import com.project.shopapp.dto.OrderResponseDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.mapper.OrderDetailMapper;
import com.project.shopapp.mapper.OrderMapper;
import com.project.shopapp.models.Order;
import com.project.shopapp.request.OrderHistoryRequest;
import com.project.shopapp.request.OrderUpdateRequest;
import com.project.shopapp.request.PurchaseRequest;
import com.project.shopapp.response.ResponseObject;
import com.project.shopapp.response.order.OrderHistoryResponse;
import com.project.shopapp.response.order.OrdersDTOListResponse;
import com.project.shopapp.response.purchase.PurchaseResponse;
import com.project.shopapp.response.utils.MessageResponse;
import com.project.shopapp.service.IOrderService;
import com.project.shopapp.service.IProductService;
import com.project.shopapp.untils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;
    private final LocalizationUtils localizationUtils;


//    @PreAuthorize("hasRole('ROLE_USER')")
//    @GetMapping("/user/{user_id}")
//    public ResponseEntity<ResponseObject> getOrders(@Valid @PathVariable("user_id") Long userId) throws DataNotFoundException {
//
//            List<Order> orders  = orderService.findByUserId(userId);
//            List<OrderDTO> orderDTOS = orders.stream()
//                    .map(
//                            order -> {
//                                OrderDTO orderDTO = OrderMapper.MAPPER.mapToOrderDTO(order);
//                                orderDTO.setUser_id(order.getUser().getId());
//                                return orderDTO;
//                            }
//                    )
//                    .collect(Collectors.toList());
//
//            return ResponseEntity.ok(ResponseObject.builder()
//                            .message(localizationUtils.getLocalizeMessage(MessageKeys.GET_ORDER_SUCCESSFULLY))
//                            .data(orderDTOS)
//                            .status(HttpStatus.OK.value())
//                    .build());
//
//    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getOrder(@Valid @PathVariable("id") Long orderId) throws DataNotFoundException {

            OrderResponseDTO existingOrder = orderService.getOrder(orderId);
            return ResponseEntity.ok(ResponseObject.builder()
                            .data(existingOrder)
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.GET_ORDER_SUCCESSFULLY))
                            .status(HttpStatus.OK.value())
                    .build());

    }
    //cong viec cua nguoi quan tri

    @PutMapping("/{orderId}")
    public ResponseEntity<ResponseObject> updateOrder(@Valid @PathVariable("orderId") Long id, @RequestBody OrderUpdateRequest orderUpdateRequest) throws DataNotFoundException {

            orderService.updateOrder(id,orderUpdateRequest);
            return ResponseEntity.ok(ResponseObject.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.UPDATE_ORDER_SUCCESSFULLY,id))
                            .data(orderUpdateRequest)
                            .status(HttpStatus.OK.value())
                    .build());

    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ResponseObject> deleteOrders(@Valid @PathVariable("orderId") Long id) throws DataNotFoundException{
        // xoa mem => cap nhat truong active = false
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ResponseObject.builder()
                .message(localizationUtils.getLocalizeMessage(MessageKeys.DELETE_ORDER_SUCCESSFULLY,id))
                .status(HttpStatus.OK.value())
                .build()
        );
    }


    @GetMapping("/order-history/{user_id}")
    public ResponseEntity<ResponseObject> getOrderHistory(
                                @PathVariable("user_id") Long userId,
                                @RequestParam(defaultValue = "",name="status") String status,
                                @RequestParam(defaultValue = "0",name="page") int page,
                                @RequestParam(defaultValue = "10",name="limit") int limit) throws DataNotFoundException {
        PageRequest pageRequest = PageRequest.of(page,limit,Sort.by("id").ascending());
        Page<OrderHistoryDTO> orderDetailHistoryDTOS = null;

        orderDetailHistoryDTOS = orderService.getOrderHistory(status,userId,pageRequest);

        if(orderDetailHistoryDTOS.getTotalElements() == 0){
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("Không có đơn hàng nào")
                    .status(HttpStatus.OK.value())
                    .data(null)
                    .build());
        }
        Long totalElements = orderDetailHistoryDTOS.getTotalElements();
        List<OrderHistoryDTO> listOrderDetails = orderDetailHistoryDTOS.getContent();
        OrderHistoryResponse orderHistoryResponse = OrderHistoryResponse.builder()
                .orderDetails(listOrderDetails)
                .totalElements(totalElements)
                .build();

        return ResponseEntity.ok().body(ResponseObject.builder()
                        .data(orderHistoryResponse)
                        .status(HttpStatus.OK.value())
                        .message("Lấy chi tiết đơn hàng thành công.")
                .build());

    }


    @GetMapping("/get-orders")
    public ResponseEntity<ResponseObject> getAllOrder(
            @RequestParam(defaultValue = "0",name="page") int page,
            @RequestParam(defaultValue = "10",name="limit") int limit)  {
        PageRequest pageRequest = PageRequest.of(page,limit,Sort.by("id").ascending());
        Page<Order> ordersPage = orderService.getOrderByKeyword(pageRequest);
        List<OrderDTO> ordersList = ordersPage.getContent()
                .stream().map(
                order -> {
                    OrderDTO orderDTO = OrderMapper.MAPPER.mapToOrderDTO(order);
                    orderDTO.setUser_id(order.getUser().getId());
                    return orderDTO;
                }
        ).collect(Collectors.toList());
        long totalElements = ordersPage.getTotalElements();
        long totalPages = ordersPage.getTotalPages();
        OrdersDTOListResponse ordersDTOListResponse = OrdersDTOListResponse.builder()
                .orders(ordersList)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .build();

        return ResponseEntity.ok(ResponseObject.builder()
                        .message("Lấy danh sách Order thành công")
                        .status(HttpStatus.OK.value())
                        .data(ordersDTOListResponse)
                .build());

    }


}
