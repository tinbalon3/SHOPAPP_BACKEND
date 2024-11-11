package com.project.shopapp.service;

public interface IStockService {
    // Giữ hàng tạm thời trong Redis
    public boolean processOrder(Long productId, int quantity) throws InterruptedException;
    // Hoàn trả số lượng vào Redis nếu thanh toán thất bại
    public void releaseReservedStock(Long productId, int quantity);
    // Kiểm tra số lượng tồn kho
    public int getStock(Long productId);
}
