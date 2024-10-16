package com.project.shopapp.service.impl;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.controller.ProductController;
import com.project.shopapp.dto.ItemPurchaseDTO;

import com.project.shopapp.dto.CustomerDTO;
import com.project.shopapp.dto.OrderDetailHistoryDTO;
import com.project.shopapp.dto.OrderResponseDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.mapper.OrderMapper;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.request.OrderUpdateRequest;
import com.project.shopapp.request.PurchaseRequest;
import com.project.shopapp.service.IOrderService;
import com.project.shopapp.untils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final LocalizationUtils localizationUtils;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    @Override
    @Transactional
    public Order createOrder(PurchaseRequest purchaseRequest) throws Exception {
        // Tìm xem user id có tồn tại hay không
        User user = userRepository.findById(purchaseRequest.getCustomer().getUserId())
                .orElseThrow(() -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER, purchaseRequest.getCustomer().getUserId())));

        // Map PurchaseRequest to Order
        try {
            Order order =new Order();
            order.setNote( purchaseRequest.getNote() );
            order.setTotalMoney( purchaseRequest.getTotalMoney() );
            order.setShippingAddress( purchaseRequest.getShippingAddress() );
            order.setBillingAddress( purchaseRequest.getBillingAddress() );
            order.setPaymentMethod( purchaseRequest.getPaymentMethod() );
            order.setShippingMethod( purchaseRequest.getShippingMethod() );
            CustomerDTO customer = purchaseRequest.getCustomer();
            if (customer != null) {
                order.setFullName(customer.getFullName());
                order.setEmail(customer.getEmail());
                order.setPhone_number(customer.getPhone_number());
            }
            order.setUser(user);
            order.setStatus(OrderStatus.PENDING);
            String orderTrackingNumber = generateOrderTrackingNumber();
            order.setTrackingNumber(orderTrackingNumber);

            // Kiểm tra shipping date phải >= ngày hôm nay
            LocalDate shippingDate = LocalDate.now();
            if (shippingDate.isBefore(LocalDate.now())) {
                throw new DataNotFoundException("Date must be at least today !");
            }
            order.setActive(true);
            order.setShippingDate(shippingDate);

            // Kiểm tra nếu địa chỉ giao hàng và địa chỉ thanh toán giống nhau
            Address shippingAddress = purchaseRequest.getShippingAddress();
            Address billingAddress = purchaseRequest.getBillingAddress();
            if (shippingAddress.equals(billingAddress)) {
                order.setBillingAddress(shippingAddress);
            } else {
                order.setShippingAddress(shippingAddress);
                order.setBillingAddress(billingAddress);
            }

            // Lưu Order và địa chỉ vào cơ sở dữ liệu
            orderRepository.save(order);

            // Tạo danh sách OrderDetail từ các mặt hàng trong giỏ hàng
            List<OrderDetail> orderDetails = new ArrayList<>();
            for (ItemPurchaseDTO itemPurchaseDTO : purchaseRequest.getCartItems()) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);

                Long productId = itemPurchaseDTO.getId();
                int quantity = itemPurchaseDTO.getQuantity();
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_PRODUCT)));
                orderDetail.setProduct(product);
                orderDetail.setNumberOfProduct(quantity);
                Float totalMoney = quantity * product.getPrice();
                orderDetail.setTotalMoney(totalMoney);
                orderDetail.setPrice(product.getPrice());
                orderDetails.add(orderDetail);
            }
            orderDetailRepository.saveAll(orderDetails);

            return order;
        }
        catch(Exception e){
            throw new IllegalArgumentException(e.getMessage());
        }


    }

    private String generateOrderTrackingNumber() {

        //generate a random UUID number (UUID version-4)
        //for details see: https://en.wikipedia.org/wiki/Universally_unique_identifier
        return UUID.randomUUID().toString();
    }
    @Override
    public OrderResponseDTO getOrder(Long id) throws DataNotFoundException {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(id);
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_ORDER,id)));
        OrderResponseDTO orderResponseDTO = OrderMapper.MAPPER.mapToOrderResponseDTO(order);
        orderResponseDTO.setOrder_details(orderDetails);
        orderResponseDTO.setStatus(order.getStatus());
        orderResponseDTO.setUser_id(order.getUser().getId());
        return orderResponseDTO;
    }
    @Override
    @Transactional
    public Order updateOrder(Long id, OrderUpdateRequest orderUpdateRequest) throws DataNotFoundException {
        Order existingOrder = orderRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_ORDER,id))
        );
       existingOrder.setStatus(orderUpdateRequest.getStatus());
        return orderRepository.save(existingOrder);


    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(null);
        //không xóa cứng, mà hãy xóa mềm
        if(order != null){
            order.setActive(false);
            orderRepository.save(order);
        }
    }
    @Override
    public List<Order> findByUserId(Long userID) {
        List<Order> orders = orderRepository.findByUserId(userID);
        return orders;
    }

    @Override
    public Page<Order> getOrderByKeyword(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Page<OrderDetailHistoryDTO> getOrderDetailHistory(String status,Long userID, Pageable pageable) throws DataNotFoundException {
        Optional<User> user = userRepository.findById(userID);

        if(user.isPresent()){
            Long id = user.get().getId();
            Page<OrderDetailHistoryDTO> orderDetailHistoryRequests =  orderRepository.getOrderDetailHistory(status,id,pageable);

            return orderDetailHistoryRequests;
        }
       else {
           throw new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER));
        }

    }

}
