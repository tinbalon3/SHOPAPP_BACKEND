package com.project.shopapp.components;

import com.project.shopapp.models.Product;
import com.project.shopapp.service.IProductService;
import com.project.shopapp.service.impl.BaseRedisServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryInitializer extends BaseRedisServiceImpl {



    @Autowired
    private IProductService productService;



    @EventListener(ApplicationReadyEvent.class)
    public void initializeInventory() {
        List<Product> products = productService.getAllProduct(); // Lấy tất cả sản phẩm từ CSDL

        for (Product product : products) {
            loadInitialStock(product.getId().toString(), product.getStock()); // Nạp tồn kho ban đầu vào Redis
        }
    }

    public void loadInitialStock(String productId, int initialStock) {
        setInt("product:" + productId + ":stock", initialStock);
    }
}
