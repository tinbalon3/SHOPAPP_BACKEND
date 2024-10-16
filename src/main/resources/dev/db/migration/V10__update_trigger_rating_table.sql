-- Đặt delimiter để định nghĩa trigger
DELIMITER //

-- Drop the existing trigger if it exists
DROP TRIGGER IF EXISTS update_product_rating_after_insert;
//

-- Create the updated trigger for INSERT
CREATE DEFINER=`root`@`localhost` TRIGGER update_product_rating_after_insert
AFTER INSERT ON rating
FOR EACH ROW
BEGIN
    -- Cộng 1 vào number_of_rating và cộng giá trị rating vào sum_of_rating
UPDATE products
SET number_of_rating = number_of_rating + 1,
    sum_of_rating = sum_of_rating + NEW.rating
WHERE id = NEW.product_id; -- Sử dụng cột 'id' trong bảng 'products'
END;
//

-- Drop the existing trigger for DELETE if it exists
DROP TRIGGER IF EXISTS update_product_rating_after_delete;
//

-- Create the updated trigger for DELETE
CREATE DEFINER=`root`@`localhost` TRIGGER update_product_rating_after_delete
AFTER DELETE ON rating
FOR EACH ROW
BEGIN
    -- Trừ 1 khỏi number_of_rating và trừ giá trị rating khỏi sum_of_rating
UPDATE products
SET number_of_rating = number_of_rating - 1,
    sum_of_rating = sum_of_rating - OLD.rating
WHERE id = OLD.product_id; -- Sử dụng cột 'id' trong bảng 'products'
END;
//

-- Drop the existing trigger for UPDATE if it exists
DROP TRIGGER IF EXISTS update_product_rating_after_update;
//

-- Create the updated trigger for UPDATE
CREATE DEFINER=`root`@`localhost` TRIGGER update_product_rating_after_update
AFTER UPDATE ON rating
                 FOR EACH ROW
BEGIN
    -- Trừ giá trị rating cũ và cộng giá trị rating mới
UPDATE products
SET number_of_rating = number_of_rating + (NEW.rating - OLD.rating),
    sum_of_rating = sum_of_rating - OLD.rating + NEW.rating
WHERE id = NEW.product_id; -- Sử dụng cột 'id' trong bảng 'products'
END;
//

-- Đặt lại delimiter về mặc định
DELIMITER ;
