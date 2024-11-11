ALTER TABLE `orders`
    ADD COLUMN transaction_id VARCHAR(255);

-- Thêm khóa ngoại liên kết với bảng transactions
ALTER TABLE `orders`
    ADD CONSTRAINT fk_order_transactions
        FOREIGN KEY (transaction_id)
            REFERENCES transactions(id);
