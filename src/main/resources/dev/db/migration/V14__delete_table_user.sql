SELECT COUNT(*)
INTO @columnCount
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'users'
  AND TABLE_SCHEMA = 'ShopApp'
  AND COLUMN_NAME = 'verification_code';

SET @alterStatement = IF(@columnCount > 0,
    'ALTER TABLE users DROP COLUMN verification_code;',
    '');

PREPARE stmt FROM @alterStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
