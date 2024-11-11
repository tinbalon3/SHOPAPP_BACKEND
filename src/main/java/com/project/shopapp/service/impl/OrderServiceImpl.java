package com.project.shopapp.service.impl;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.*;

import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.mapper.OrderMapper;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.request.OrderUpdateRequest;
import com.project.shopapp.request.PurchaseRequest;
import com.project.shopapp.service.IOrderService;
import com.project.shopapp.service.IProductService;
import com.project.shopapp.service.IStockService;
import com.project.shopapp.untils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class OrderServiceImpl extends BaseRedisServiceImpl implements IOrderService {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  OrderRepository orderRepository;
    @Autowired
    private  LocalizationUtils localizationUtils;
    @Autowired
    private  ProductRepository productRepository;
    @Autowired
    private IProductService productService;
    @Autowired
    private  OrderDetailRepository orderDetailRepository;
    @Autowired
    private  IStockService iStockService;
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    public OrderServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    @Transactional
    public Order createOrder(PurchaseRequest purchaseRequest) throws Exception {

        // Tìm xem user id có tồn tại hay không
        User user = userRepository.findById(purchaseRequest.getCustomer().getUserId())
                .orElseThrow(() -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER, purchaseRequest.getCustomer().getUserId())));

        // Map PurchaseRequest to Order

            // Duyệt qua từng sản phẩm trong cartItems để kiểm tra tồn kho
            for (ItemPurchaseDTO item : purchaseRequest.getCartItems()) {
                boolean isAvailable = iStockService.processOrder(item.getId(), item.getQuantity());
                if (!isAvailable) {
                    throw new Exception("Sản phẩm " + item.getId() + " không đủ tồn kho.");
                }
            }

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

            String keyPurchase = "purchase:userID:"+purchaseRequest.getCustomer().getUserId();
            saveObject(keyPurchase,purchaseRequest);
            setTimeToLive(keyPurchase,900);




            return order;

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
        orderResponseDTO.setStatus(order.getStatus().toString());
        orderResponseDTO.setUser_id(order.getUser().getId());
        return orderResponseDTO;
    }
    @Override
    @Transactional
    public Order updateOrder(Long id, OrderUpdateRequest orderUpdateRequest) throws DataNotFoundException {
        Order existingOrder = orderRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_ORDER,id))
        );
       existingOrder.setStatus(OrderStatus.valueOf(orderUpdateRequest.getStatus()));
        return orderRepository.save(existingOrder);


    }

    @Override
    @Transactional
    public void deleteOrder(Long id) throws DataNotFoundException {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_ORDER))
        );
        //không xóa cứng, mà hãy xóa mềm
        if(order != null){
            order.setActive(false);
            orderRepository.save(order);
        }
    }
    @Override
    public List<Order> findByUserId(Long userID) throws DataNotFoundException {
        try {
            List<Order> orders = orderRepository.findByUserId(userID);
            return orders;
        } catch (Exception e){
            throw new DataNotFoundException("Not found user id ");
        }

    }

    @Override
    public Order findByTransactionId(String transactionId) throws DataNotFoundException {
        try {
            Order order = orderRepository.findByTransactionId(transactionId);
            return order;
        } catch (Exception e){
            throw new DataNotFoundException("Not found transaction id");
        }

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

    @Transactional
    public void updateStockAndOrder(Order order) throws Exception {
        // Cập nhật trạng thái đơn hàng
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        String keyPurchase = "purchase:userID:"+order.getUser().getId();
        PurchaseRequest purchaseRequest = getObject(keyPurchase,PurchaseRequest.class);

        for (ItemPurchaseDTO item : purchaseRequest.getCartItems()) {
            Product product = productService.getProductById(item.getId());
            int stockProduct = product.getStock();

            // Kiểm tra tồn kho
            if (stockProduct < item.getQuantity()) {
                throw new Exception("Không đủ hàng cho sản phẩm: " + product.getName());
            }

            // Trừ số lượng trong cơ sở dữ liệu
            product.setStock(stockProduct - item.getQuantity());
            productRepository.save(product);
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
           iStockService.releaseReservedStock(item.getId(), item.getQuantity());
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
