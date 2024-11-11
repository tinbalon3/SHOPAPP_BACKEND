package com.project.shopapp.service.impl;

import com.project.shopapp.service.IBaseRedisService;
import com.project.shopapp.service.IStockService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class StockServiceImpl extends BaseRedisServiceImpl implements IStockService {
    public StockServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }
    @Override
    public boolean processOrder(Long productId, int quantity) throws InterruptedException {
        String lockKey = "lock:" + productId;
        int maxRetries = 5;
        if (tryLockWithRetry(lockKey, maxRetries)) {
            try {
                // Kiểm tra tồn kho
                if (!reserveStock(productId,quantity)) {
                    return false;  // Thông báo hết hàng
                }

                return true;  // Thành công
            } finally {
                delete(lockKey);  // Giải phóng khóa
            }
        } else {
            return false;  // Không thể lấy khóa sau nhiều lần thử
        }
    }

    private boolean reserveStock(Long productId, int quantity) {

        String stockKey = "product:" + productId + ":stock";
        int stockQuantity = getStock(productId);
        if (stockQuantity != 0 && stockQuantity >= quantity) {
            // Giảm số lượng trong Redis
            decrementBy(stockKey, quantity);
            return true;
        }
        return false; // Nếu số lượng không đủ
    }
    private boolean tryLockWithRetry(String lockKey,int maxRetries) throws InterruptedException {
        for (int i = 0; i < maxRetries; i++) {
            if (redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(2))) {
                return true;  // Lấy được khóa
            }
            // Nếu không lấy được khóa, chờ một chút rồi thử lại
            Thread.sleep(100);
        }
        return false;  // Không thể lấy khóa sau nhiều lần thử
    }

    @Override
    public void releaseReservedStock(Long productId, int quantity) {
        String stockKey = "product:" + productId + ":stock";
        incrementBy(stockKey, quantity);
    }

    @Override
    public int getStock(Long productId) {
        String stockKey = "product:" + productId + ":stock";
        Optional<Integer> stockQuantity = Optional.ofNullable((Integer) get(stockKey));
        return stockQuantity.orElse(0);
    }
}
