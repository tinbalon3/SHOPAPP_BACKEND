package com.project.shopapp.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.dto.ItemPurchaseDTO;
import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.dto.PaymentObject;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.mapper.OrderMapper;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderStatus;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.request.PurchaseRequest;
import com.project.shopapp.service.IOrderService;
import com.project.shopapp.service.IProductService;
import com.project.shopapp.service.IStockService;
import com.project.shopapp.service.IUserService;
import com.project.shopapp.service.impl.BaseRedisServiceImpl;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KafkaPaymentConsumner extends BaseRedisServiceImpl{
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IStockService stockService;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    public KafkaPaymentConsumner(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }







    @KafkaListener(id = "notifyEmail",topics = "failed_topic",concurrency = "3")
    public void notifyEmailFailToUser(Order order) throws DataNotFoundException {
        Optional<User> user = iUserService.getUserById(order.getUser().getId());
        if(user.isPresent()){
            String emailOfUserOrigin = user.get().getEmail();
            OrderDTO orderDTO = OrderMapper.MAPPER.mapToOrderDTO(order);
            orderDTO.setUser_id(order.getUser().getId());
            orderDTO.setEmail(emailOfUserOrigin);
            iUserService.sendErrorMailOnInvalidEmail(orderDTO);
        }

    }
    @KafkaListener(topics = "order-payments-success", id = "orderPaymentsSuccess")
    public void orderPaymentsSuccess(Long userID) throws Exception {
        String orderKeyUser = "order:user:" + userID;
        Object value = get(orderKeyUser); // lấy giá trị từ Redis
        Long orderId = (value instanceof Integer) ? ((Integer) value).longValue() : (Long) value;

        Order order = orderService.findByOrderId(orderId);
        orderService.updateStockAndOrder(order);

        // Sau khi cập nhật đơn hàng thành công, gửi message tới topic gửi email
        kafkaTemplate.send("order-updated-success", orderId);
    }
    @KafkaListener(topics = "order-updated-success", id = "sendOrderEmail")
    public void sendOrderEmail(Long orderId) throws DataNotFoundException {
        Order order = orderService.findByOrderId(orderId);
        OrderDTO orderDTO = OrderMapper.MAPPER.mapToOrderDTO(order);
        orderDTO.setUser_id(order.getUser().getId());

        try {
            iUserService.sendMailOrderSuccessfully(orderDTO);
        } catch (Exception e) {
            retryMessage(orderDTO, "retry_5m_topic", 1, 5 * 60 * 1000);  // Retry sau 5 phút nếu gửi email thất bại
        }
    }


    @KafkaListener(topics = "order-payments-fail",id = "orderPaymentsFail")
    public void orderPaymentsFail(Long userID) throws JsonProcessingException, DataNotFoundException {

        String keyPurchase = "purchase:userID:" + userID;
        PurchaseRequest purchaseRequest = getObject(keyPurchase,PurchaseRequest.class);

        String orderKeyUser = "order:user:" + userID;
        Long orderId = (Long) get(orderKeyUser);
        Order order = orderService.findByOrderId(orderId);

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        for (ItemPurchaseDTO item : purchaseRequest.getCartItems()) {
            stockService.releaseReservedStock(item.getId(), item.getQuantity());
        }
        System.out.println("Xử lý thanh toán thất bại và cập nhật tồn kho trên redis thành công.");
    }
    private void retryMessage(OrderDTO order, String topic, int retryNumber, long delay) {
        order.setRetryCount(retryNumber);
        order.setRetryTimestamp(System.currentTimeMillis() + delay);
        kafkaTemplate.send(topic, order);
    }
}
