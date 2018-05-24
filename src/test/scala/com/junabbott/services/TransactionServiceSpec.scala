package com.junabbott.services

import org.scalatest.{ BeforeAndAfterAll, Matchers, OptionValues, WordSpec }
import scalikejdbc._
import scalikejdbc.config.DBs

class TransactionServiceSpec extends WordSpec with Matchers with OptionValues with BeforeAndAfterAll {

  val transactionService = new TransactionService('transaction)
  val accountService = new AccountService('transaction)
  override def beforeAll: Unit = {
    DBs.setup('transaction)

    NamedDB('transaction) localTx { implicit session =>
      sql"""CREATE TABLE IF NOT EXISTS account(
        id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        create_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        )""".update().apply()
      sql"""CREATE TABLE IF NOT EXISTS journal(
        id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        account_id INT NOT NULL,
        transaction_type VARCHAR(50) NOT NULL,
        create_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        amount FLOAT NOT NULL,
        CONSTRAINT `fk_journal_account`
          FOREIGN KEY (account_id) REFERENCES account (id)
          ON DELETE CASCADE
          ON UPDATE RESTRICT
      )""".update().apply()
      sql"""CREATE TABLE IF NOT EXISTS principal_ledger(
        id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        account_id INT NOT NULL,
        debit FLOAT,
        credit FLOAT,
        CONSTRAINT `fk_principal_ledger_account`
          FOREIGN KEY (account_id) REFERENCES account (id)
          ON DELETE CASCADE
          ON UPDATE RESTRICT
      )""".update().apply()
      sql"""CREATE TABLE IF NOT EXISTS cash_out_ledger(
        id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        account_id INT NOT NULL,
        debit FLOAT,
        credit FLOAT,
        CONSTRAINT `fk_cash_out_ledger_account`
          FOREIGN KEY (account_id) REFERENCES account (id)
          ON DELETE CASCADE
          ON UPDATE RESTRICT
      )""".update().apply()
    }
    accountService.addAccount()
  }

  override def afterAll: Unit = {
    DBs.close('transaction)
  }

  "TransactionService" should {
    "be able to add purchase transaction" in {
      transactionService.addPurchaseTransaction(1, 50)
      val amount = NamedDB('transaction) readOnly { implicit session =>
        sql"select amount from journal where account_id = 1"
          .map(rs => rs.double("amount"))
          .single().apply()
      }
      amount.value shouldEqual 50.0
    }
    "be able to get transactions" in {
      val res = transactionService.getTransactions(1)
      res should have size 1
      res.head.id shouldEqual 1
      res.head.amount shouldEqual 50
    }
    "be able to get principal" in {
      transactionService.getPrincipal(1).value shouldEqual 50
    }
  }
}
