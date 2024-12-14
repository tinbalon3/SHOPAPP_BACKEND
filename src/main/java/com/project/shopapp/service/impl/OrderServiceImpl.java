package com.project.shopapp.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.*;

import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.OrderException;
import com.project.shopapp.exceptions.ProductOutOfStockException;
import com.project.shopapp.mapper.OrderMapper;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.request.OrderUpdateRequest;
import com.project.shopapp.request.PurchaseRequest;
import com.project.shopapp.service.*;
import com.project.shopapp.untils.MessageKeys;
import com.project.shopapp.untils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl extends BaseRedisServiceImpl implements IOrderService {
    @Autowired
    private IUserService userService;

    @Autowired
    private  OrderRepository orderRepository;

    @Autowired
    private  LocalizationUtils localizationUtils;

    @Autowired
    private ObjectMapper redisObjectMapper;

    @Autowired
    private IProductService productService;

    @Autowired
    private IOrderDetailService orderDetailService;

    @Autowired
    private  IStockService stockService;
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);


    @Override
    @Transactional
    public void createOrder(PurchaseRequest purchaseRequest) throws Exception {

        // Tìm xem user id có tồn tại hay không
        User user = userService.getUserById(purchaseRequest.getCustomer().getUserId())
                .orElseThrow(() -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER, purchaseRequest.getCustomer().getUserId())));
        if(!ValidationUtils.validatePhoneNumber(purchaseRequest.getCustomer().getPhone_number())) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        }
        // Map PurchaseRequest to Order
        try {
            for (ItemPurchaseDTO item : purchaseRequest.getCartItems()) {
                boolean isAvailable = stockService.processOrder(item.getId(), item.getQuantity());
                if (!isAvailable) {
                    throw new ProductOutOfStockException("Quá số lượng tồn kho:" + item.getId());
                }
            }

            Order order =new Order();
            order.setNote( purchaseRequest.getNote() );
            order.setTotalMoney( purchaseRequest.getTotalMoney() );
            order.setShippingAddress( purchaseRequest.getShippingAddress() );
//            order.setBillingAddress( purchaseRequest.getBillingAddress() );
            order.setPaymentMethod( purchaseRequest.getPaymentMethod() );
            order.setShippingMethod( purchaseRequest.getShippingMethod() );
            CustomerDTO customer = purchaseRequest.getCustomer();
            if (customer != null) {
                order.setFullName(customer.getFullName());
                order.setEmail(customer.getEmail());
                order.setPhone_number(customer.getPhone_number());
            }
            order.setUser(user);
            order.setStatus(OrderStatus.RESERVED);
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
//            Address shippingAddress = purchaseRequest.getShippingAddress();
//            Address billingAddress = purchaseRequest.getBillingAddress();
//            if (shippingAddress.equals(billingAddress)) {
//                order.setBillingAddress(shippingAddress);
//            } else {
//                order.setShippingAddress(shippingAddress);
//                order.setBillingAddress(billingAddress);
//            }

            // Lưu Order và địa chỉ vào cơ sở dữ liệu
//            saveOrder(order);

            // Tạo danh sách OrderDetail từ các mặt hàng trong giỏ hàng
            List<OrderDetail> orderDetails = new ArrayList<>();
            for (ItemPurchaseDTO itemPurchaseDTO : purchaseRequest.getCartItems()) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);

                Long productId = itemPurchaseDTO.getId();
                int quantity = itemPurchaseDTO.getQuantity();
                Product product = productService.getProductById(productId);
                orderDetail.setProduct(product);
                orderDetail.setNumberOfProduct(quantity);
                Float totalMoney = quantity * product.getPrice();
                orderDetail.setTotalMoney(totalMoney);
                orderDetail.setPrice(product.getPrice());
                orderDetails.add(orderDetail);
            }
//            orderDetailService.saveAllOrderDetail(orderDetails);
//            order.setOrderDetails(orderDetails);
            String hashKey = "orderPayment:user:" + purchaseRequest.getCustomer().getUserId();
            hashSet(hashKey, "order", order);
            hashSet(hashKey, "orderDetails", orderDetails);
            hashSet(hashKey, "purchaseRequest", purchaseRequest);
//            String keyOrderDetail = "order_detail:userID:"+purchaseRequest.getCustomer().getUserId();
//            String keyPurchase = "purchase:userID:"+purchaseRequest.getCustomer().getUserId();
//            String keyOrder = "order:userID:"+purchaseRequest.getCustomer().getUserId();
//            saveObject(keyOrder,order);
//            setTimeToLive(keyOrder,900);
//            saveObject(keyOrderDetail,orderDetails);
//            setTimeToLive(keyPurchase,900);
//            saveObject(keyPurchase,purchaseRequest);
//            setTimeToLive(keyPurchase,900);
        }
        catch (Exception e) {
            for (ItemPurchaseDTO item : purchaseRequest.getCartItems()) {
                stockService.releaseReservedStock(item.getId(), item.getQuantity());
            }
            throw new OrderException("Xử lý thanh toán thất bại do lỗi hệ thống. Vui lòng thử lại sau.");
        }




    }

    private String generateOrderTrackingNumber() {

        //generate a random UUID number (UUID version-4)
        //for details see: https://en.wikipedia.org/wiki/Universally_unique_identifier
        return UUID.randomUUID().toString();
    }
    @Override
    public OrderResponseDTO getOrder(Long id) throws DataNotFoundException {
        List<OrderDetail> orderDetails = orderDetailService.findByOrderId(id);
        Order order = findByOrderId(id);
        OrderResponseDTO orderResponseDTO = OrderMapper.MAPPER.mapToOrderResponseDTO(order);
        orderResponseDTO.setOrder_details(orderDetails);
        orderResponseDTO.setStatus(order.getStatus().toString());
        orderResponseDTO.setUser_id(order.getUser().getId());
        return orderResponseDTO;
    }
    @Override
    @Transactional
    public void updateOrder(Long id, OrderUpdateRequest orderUpdateRequest) throws DataNotFoundException {
        Order existingOrder = findByOrderId(id);

       existingOrder.setStatus(OrderStatus.valueOf(orderUpdateRequest.getStatus()));
       saveOrder(existingOrder);


    }

    @Override
    @Transactional
    public void deleteOrder(Long id) throws DataNotFoundException {
        Order order =  findByOrderId(id);
        //không xóa cứng, mà hãy xóa mềm
        if(order != null){
            order.setActive(false);
            saveOrder(order);
        }
    }
    @Override
    public List<Order> findByUserId(Long userID) throws DataNotFoundException {
        try {
            List<Order> orders = findByUserId(userID);
            return orders;
        } catch (Exception e){
            throw new DataNotFoundException("Not found user id ");
        }

    }

    @Override
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }


    @Override
    public Page<Order> getOrderByKeyword(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Page<OrderHistoryDTO> getOrderHistory(String status,Long userID, Pageable pageable) throws DataNotFoundException {
        Optional<User> user = userService.getUserById(userID);

        if (user.isPresent()) {
            Long id = user.get().getId();
            OrderStatus orderStatus = null;

            if (status != "" && !status.isEmpty()) {
                try {
                    orderStatus = OrderStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
                }
            }

            List<Object[]> orderDetailHistoryRequests = orderRepository.getOrderHistory(orderStatus, id, pageable);
            List<OrderHistoryDTO> orderHistoryDTOs = new ArrayList<>();

            for (Object[] result : orderDetailHistoryRequests) {
                Long idp = (Long) result[0];
                LocalDateTime orderDate = ((Timestamp) result[1]).toLocalDateTime();

                OrderStatus status1 = (OrderStatus) result[2]; // Enum chuyển về String
                String paymentMethod = (String) result[3];
                Float totalMoney = (Float) result[4];
                Long totalProducts = (Long) result[5];

                // Tạo OrderHistoryDTO từ dữ liệu trong Object[]
                OrderHistoryDTO dto =  OrderHistoryDTO.builder()
                        .orderDate(orderDate)
                        .id(idp)
                        .status(status1)
                        .paymentMethod(paymentMethod)
                        .totalMoney(totalMoney)
                        .totalProducts(totalProducts)
                        .build();
                orderHistoryDTOs.add(dto);
            }
            return new PageImpl<>(orderHistoryDTOs, pageable, orderHistoryDTOs.size());

        }

        else {
           throw new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER));
        }

    }

    @Transactional
    public void updateStockAndOrder(Long userID) throws Exception {
        // Cập nhật trạng thái đơn hàng

        String hashKey = "orderPayment:user:" + userID;

        PurchaseRequest purchaseRequest = hashGetObject(hashKey,"purchaseRequest",PurchaseRequest.class);

        for (ItemPurchaseDTO item : purchaseRequest.getCartItems()) {
            Product product = productService.getProductById(item.getId());
            int stockProduct = product.getStock();

            // Kiểm tra tồn kho
            if (stockProduct < item.getQuantity()) {
                throw new Exception("Không đủ hàng cho sản phẩm: " + product.getName());
            }

            // Trừ số lượng trong cơ sở dữ liệu
            product.setStock(stockProduct - item.getQuantity());
            productService.saveProduct(product);
        }
        System.out.println("Cập nhật đơn hàng và tồn kho trong MySQL thành công.");
    }

    @Transactional
    public void revertStockAndOrder(PaymentObject paymentObject) throws DataNotFoundException {
        // Cập nhật trạng thái đơn hàng thành "cancelled"
        paymentObject.getOrder().setStatus(OrderStatus.CANCELED);
        orderRepository.save(paymentObject.getOrder());

        PurchaseRequest purchaseRequest = paymentObject.getPurchaseRequest();
        for (ItemPurchaseDTO item : purchaseRequest.getCartItems()) {
           stockService.releaseReservedStock(item.getId(), item.getQuantity());
        }
        System.out.println("Thanh toán thất bại, cập nhật trạng thái đơn hàng và khôi phục tồn kho trong MySQL thành công.");
    }



    @Override
    public Order findByOrderId(Long orderId) throws DataNotFoundException {
            Order order = orderRepository.findById(orderId).orElseThrow(
                    () -> new DataNotFoundException("Not found transaction id"));
            return order;

    }

}
