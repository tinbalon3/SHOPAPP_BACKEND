SELECT COUNT(*)
INTO @columnCount
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'tokens'
  AND TABLE_SCHEMA = 'ShopApp'
  AND COLUMN_NAME = 'is_mobile';

SET @alterStatement = IF (@columnCount = 0,
                          'ALTER TABLE tokens ADD COLUMN is_mobile TINYINT(1) DEFAULT 0;',
                          '');

PREPARE stmt FROM @alterStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;