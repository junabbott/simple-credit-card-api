package com.junabbott.services

import com.junabbott.actors.TransactionActor.Transaction
import scalikejdbc._

class TransactionService(dbName: Symbol = 'default) {

  def addPurchaseTransaction(accountId: Int, amount: Double): Int = {
    NamedDB(dbName) localTx { implicit session =>
      sql"insert into journal (account_id, transaction_type, amount) values ($accountId, 'purchase', $amount)".update().apply()
      sql"insert into principal_ledger (account_id, debit, credit) values ($accountId, $amount, 0)".update().apply()
      sql"insert into cash_out_ledger (account_id, debit, credit) values ($accountId, 0, $amount)".update().apply()
    }
  }

  def getTransactions(accountId: Int): List[Transaction] = {
    NamedDB(dbName) readOnly { implicit session =>
      sql"select id, transaction_type, create_datetime, amount from journal where account_id = ${accountId}"
        .map(rs => Transaction(rs.int("id"), rs.string("transaction_type"), rs.dateTime("create_datetime"), rs.double("amount")))
        .list.apply()
    }
  }

  def getPrincipal(accountId: Int): Option[Double] = {
    NamedDB(dbName) readOnly { implicit session =>
      sql"select sum(debit) as debit, sum(credit) as credit from principal_ledger group by account_id having account_id = ${accountId}"
        .map(rs => rs.double("debit") - rs.double("credit"))
        .single.apply()
    }
  }
}
