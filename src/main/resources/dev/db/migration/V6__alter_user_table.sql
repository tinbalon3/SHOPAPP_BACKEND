SELECT COUNT(*)
INTO @columnCount
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'users'
  AND TABLE_SCHEMA = 'ShopApp'
  AND COLUMN_NAME = 'verification_code';

SET @alterStatement = IF(@columnCount = 0,
    'ALTER TABLE users ADD COLUMN verification_code VARCHAR(64) DEFAULT '''';',
    '');

PREPARE stmt FROM @alterStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*)
INTO @columnCount
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'users'
  AND TABLE_SCHEMA = 'ShopApp'
  AND COLUMN_NAME = 'enabled';

SET @alterStatement = IF(@columnCount = 0,
    'ALTER TABLE users ADD COLUMN enabled TINYINT(1) DEFAULT 0;',
    '');

PREPARE stmt FROM @alterStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*)
INTO @columnCount
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'users'
  AND TABLE_SCHEMA = 'ShopApp'
  AND COLUMN_NAME = 'email';

SET @alterStatement = IF(@columnCount = 0,
    'ALTER TABLE users ADD COLUMN email VARCHAR(255) NOT NULL;',
    '');

PREPARE stmt FROM @alterStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;