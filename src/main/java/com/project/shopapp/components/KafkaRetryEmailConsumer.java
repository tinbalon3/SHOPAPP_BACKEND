package com.project.shopapp.components;

import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class KafkaRetryEmailConsumer {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // Executor service để quản lý các luồng xử lý song song
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @KafkaListener(id = "retry5mListener", topics = "retry_5m_topic", concurrency = "3")
    public void handleRetry5m(OrderDTO order) {
        if (order.getRetryCount() < 2) { // Retry 2 lần với delay 5 phút
            processRetryAsync(order, order.getRetryCount() + 1, "retry_5m_topic", 5 * 60 * 1000);
        } else {
            processRetryAsync(order, 3, "retry_30m_topic", 30 * 60 * 1000); // Chuyển sang retry 30 phút sau 2 lần retry 5 phút
        }
    }

    @KafkaListener(id = "retry30mListener", topics = "retry_30m_topic", concurrency = "3")
    public void handleRetry30m(OrderDTO order) {
        if (order.getRetryCount() < 5) { // Retry 3 lần với delay 30 phút
            processRetryAsync(order, order.getRetryCount() + 1, "retry_30m_topic", 30 * 60 * 1000);
        } else {
            processRetryAsync(order, 6, "retry_1h_topic", 60 * 60 * 1000); // Chuyển sang retry 1 giờ sau 3 lần retry 30 phút
        }
    }

    @KafkaListener(id = "retry1hListener", topics = "retry_1h_topic", concurrency = "3")
    public void handleRetry1h(OrderDTO order) {
        processRetryAsync(order, 6, "failed_topic", 0); // Retry lần cuối, nếu lỗi thì chuyển vào failed_topic
    }

    // Hàm retry message vào topic mới
    private void retryMessage(OrderDTO order, String topic, int retryNumber, long delay) {
        order.setRetryCount(retryNumber);
        order.setRetryTimestamp(System.currentTimeMillis() + delay);
        kafkaTemplate.send(topic, order);
    }

    // Hàm xử lý retry song song (asynchronously)
    private void processRetryAsync(OrderDTO order, int retryCount, String nextTopic, long delay) {
        executorService.submit(() -> {
            // Kiểm tra thời gian retry, nếu cần đợi
            if (System.currentTimeMillis() < order.getRetryTimestamp()) {
                try {
                    Thread.sleep(order.getRetryTimestamp() - System.currentTimeMillis());
                } catch (InterruptedException ignored) {}
            }

            // Thử gửi email
            try {
                iUserService.sendMailOrderSuccessfully(order);
            } catch (Exception e) {
                if (retryCount < 6) {
                    retryMessage(order, nextTopic, retryCount, delay); // Nếu lỗi, tiếp tục retry với topic và delay mới
                } else {
                    kafkaTemplate.send("failed_topic", order); // Nếu đã retry đủ lần, chuyển vào failed_topic
                }
            }
        });
    }
}
