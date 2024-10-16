CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR(255) PRIMARY KEY ,
    bill_no VARCHAR(255),
    trans_no VARCHAR(255),
    bank_code VARCHAR(255),
    card_type VARCHAR(255),
    amount INT,
    currency VARCHAR(50),
    bank_account_no VARCHAR(255),
    bank_account VARCHAR(255),
    refund_bank_code VARCHAR(255),
    reason TEXT,
    create_date TIMESTAMP,
    status VARCHAR(50)
    );

ALTER TABLE transactions
ADD COLUMN order_id INT,
ADD CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;

ALTER TABLE transactions
ADD COLUMN user_id INT,
ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;