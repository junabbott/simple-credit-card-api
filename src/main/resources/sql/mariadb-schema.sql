CREATE TABLE IF NOT EXISTS account(
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  create_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS journal(
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  account_id INT NOT NULL,
  transaction_type VARCHAR(50) NOT NULL,
  create_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  amount FLOAT NOT NULL,
  CONSTRAINT `fk_journal_account`
    FOREIGN KEY (account_id) REFERENCES account (id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS principal_ledger(
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  account_id INT NOT NULL,
  debit FLOAT,
  credit FLOAT,
  CONSTRAINT `fk_principal_ledger_account`
    FOREIGN KEY (account_id) REFERENCES account (id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS cash_out_ledger(
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  account_id INT NOT NULL,
  debit FLOAT,
  credit FLOAT,
  CONSTRAINT `fk_cash_out_ledger_account`
    FOREIGN KEY (account_id) REFERENCES account (id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT
);

-- drop table principal_ledger;
-- drop table cash_out_ledger;
-- drop table journal;
-- drop table account;