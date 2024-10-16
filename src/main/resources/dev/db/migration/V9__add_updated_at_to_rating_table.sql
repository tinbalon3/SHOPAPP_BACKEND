-- Thêm cột updated_at vào bảng rating nếu chưa tồn tại
ALTER TABLE rating
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
