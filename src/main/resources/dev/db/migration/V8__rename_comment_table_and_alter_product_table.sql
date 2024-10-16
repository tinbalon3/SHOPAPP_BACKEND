-- Thêm cột number_of_rating nếu chưa tồn tại
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS number_of_rating INT(11) DEFAULT 0;

-- Thêm cột sum_of_rating nếu chưa tồn tại
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS sum_of_rating INT(11) DEFAULT 0;

-- Thêm cột rating nếu chưa tồn tại
ALTER TABLE rating
    ADD COLUMN IF NOT EXISTS rating INT DEFAULT 0;

-- Đổi DELIMITER để định nghĩa trigger
DELIMITER $$

-- Tạo trigger sau khi chèn vào bảng rating
CREATE TRIGGER update_product_rating_after_insert
    AFTER INSERT ON rating
    FOR EACH ROW
BEGIN
    -- Cộng 1 vào number_of_rating và cộng giá trị rating vào sum_of_rating
    UPDATE products
    SET number_of_rating = number_of_rating + 1,
        sum_of_rating = sum_of_rating + NEW.rating
    WHERE product_id = NEW.product_id;
END $$

-- Tạo trigger sau khi xóa khỏi bảng rating
CREATE TRIGGER update_product_rating_after_delete
    AFTER DELETE ON rating
    FOR EACH ROW
BEGIN
    -- Trừ 1 khỏi number_of_rating và trừ giá trị rating khỏi sum_of_rating
    UPDATE products
    SET number_of_rating = number_of_rating - 1,
        sum_of_rating = sum_of_rating - OLD.rating
    WHERE product_id = OLD.product_id;
END $$

-- Tạo trigger sau khi cập nhật bảng rating
CREATE TRIGGER update_product_rating_after_update
    AFTER UPDATE ON rating
    FOR EACH ROW
BEGIN
    -- Trừ giá trị rating cũ và cộng giá trị rating mới
    UPDATE products
    SET sum_of_rating = sum_of_rating - OLD.rating + NEW.rating
    WHERE product_id = NEW.product_id;
END $$

-- Đặt lại DELIMITER về mặc định
DELIMITER ;
