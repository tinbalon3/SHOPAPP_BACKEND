package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;

import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.dto.OrderDetailHistoryDTO;
import com.project.shopapp.dto.OrderResponseDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.mapper.OrderDetailMapper;
import com.project.shopapp.mapper.OrderMapper;
import com.project.shopapp.models.Order;
import com.project.shopapp.request.OrderDetailHistoryRequest;
import com.project.shopapp.request.OrderUpdateRequest;
import com.project.shopapp.request.PurchaseRequest;
import com.project.shopapp.response.ResponseObject;
import com.project.shopapp.response.order.OrderDetailHistoryResponse;
import com.project.shopapp.response.order.OrdersDTOListResponse;
import com.project.shopapp.response.purchase.PurchaseResponse;
import com.project.shopapp.response.utils.MessageResponse;
import com.project.shopapp.service.IOrderService;
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

    private final IOrderService iOrderService;
    private final LocalizationUtils localizationUtils;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @PostMapping("")
    public ResponseEntity<ResponseObject> createOrder(@RequestBody @Valid PurchaseRequest purchaseRequest, BindingResult result) throws Exception {

            if(result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(ResponseObject.builder()
                                .message(String.join(";",errorMessage))
                                .status(HttpStatus.BAD_REQUEST)
                        .build());
            }

            Order order = iOrderService.createOrder(purchaseRequest);
            PurchaseResponse purchaseResponse = new PurchaseResponse(order.getTrackingNumber());
            return ResponseEntity.ok().body(ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .data(purchaseResponse)
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.CREATE_ORDER_SUCCESSFULLY))
                    .build());

    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<ResponseObject> getOrders(@Valid @PathVariable("user_id") Long userId) throws DataNotFoundException {

            List<Order> orders  = iOrderService.findByUserId(userId);
            List<OrderDTO> orderDTOS = orders.stream()
                    .map(
                            order -> {
                                OrderDTO orderDTO = OrderMapper.MAPPER.mapToOrderDTO(order);
                                orderDTO.setUser_id(order.getUser().getId());
                                return orderDTO;
                            }
                    )
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ResponseObject.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.GET_ORDER_SUCCESSFULLY))
                            .data(orderDTOS)
                            .status(HttpStatus.OK)
                    .build());

    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getOrder(@Valid @PathVariable("id") Long orderId) throws DataNotFoundException {

            OrderResponseDTO existingOrder = iOrderService.getOrder(orderId);

            return ResponseEntity.ok(ResponseObject.builder()
                            .data(existingOrder)
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.GET_ORDER_SUCCESSFULLY))
                            .status(HttpStatus.OK)
                    .build());

    }
    //cong viec cua nguoi quan tri

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateOrder(@Valid @PathVariable Long id, @RequestBody OrderUpdateRequest orderUpdateRequest) throws DataNotFoundException {

            Order order = iOrderService.updateOrder(id,orderUpdateRequest);
            return ResponseEntity.ok(ResponseObject.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.UPDATE_ORDER_SUCCESSFULLY,id))
                            .data(order)
                            .status(HttpStatus.OK)
                    .build());

    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteOrders(@Valid @PathVariable Long id) throws DataNotFoundException{
        // xoa mem => cap nhat truong active = false
        iOrderService.deleteOrder(id);
        return ResponseEntity.ok(ResponseObject.builder()
                .message(localizationUtils.getLocalizeMessage(MessageKeys.DELETE_ORDER_SUCCESSFULLY,id))
                .status(HttpStatus.OK)
                .build()
        );
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/order-history/{user_id}")
    public ResponseEntity<ResponseObject> getOrderDetailHistory(
                                @PathVariable("user_id") Long userId,
                                @RequestParam(defaultValue = "",name="status") String status,
                                @RequestParam(defaultValue = "0",name="page") int page,
                                @RequestParam(defaultValue = "10",name="limit") int limit) throws DataNotFoundException {
        PageRequest pageRequest = PageRequest.of(page,limit,Sort.by("id").ascending());
        Page<OrderDetailHistoryDTO> orderDetailHistoryDTOS = null;

        orderDetailHistoryDTOS = iOrderService.getOrderDetailHistory(status,userId,pageRequest);

        if(orderDetailHistoryDTOS.getTotalElements() == 0){
            return null;
        }
        Long totalElements = orderDetailHistoryDTOS.getTotalElements();
        List<OrderDetailHistoryDTO> listOrderDetails = orderDetailHistoryDTOS.getContent();
        List<OrderDetailHistoryRequest> orderDetailHistoryRequests = listOrderDetails.stream().map(orderDetailHistoryDTO -> {
            OrderDetailHistoryRequest orderDetailHistoryRequest = OrderDetailMapper.MAPPER.mapToOrderDetailHistoryRequest(orderDetailHistoryDTO);

            return orderDetailHistoryRequest;
        }).collect(Collectors.toList());
        if (orderDetailHistoryDTOS == null || orderDetailHistoryDTOS.isEmpty()) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("Order history not found for user ID: " + userId)
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
        OrderDetailHistoryResponse orderDetailHistoryResponse = OrderDetailHistoryResponse.builder()
                .orderDetails(orderDetailHistoryRequests)
                .totalElements(totalElements)
                .build();

        return ResponseEntity.ok().body(ResponseObject.builder()
                        .data(orderDetailHistoryResponse)
                        .status(HttpStatus.OK)
                        .message("Lấy chi tiết đơn hàng thành công")
                .build());

    }


    @GetMapping("/get-orders")
    public ResponseEntity<ResponseObject> getAllOrder( @RequestParam(defaultValue = "0",name="page") int page,
                                          @RequestParam(defaultValue = "10",name="limit") int limit)  {
        PageRequest pageRequest = PageRequest.of(page,limit,Sort.by("id").ascending());
        Page<Order> ordersPage = iOrderService.getOrderByKeyword(pageRequest);
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
                        .status(HttpStatus.OK)
                        .data(ordersDTOListResponse)
                .build());

    }


}
