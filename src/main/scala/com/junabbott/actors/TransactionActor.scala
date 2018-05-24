package com.junabbott.actors

import java.time.ZonedDateTime

import akka.actor.{ Actor, ActorLogging, Props }
import com.junabbott.actors.AccountActor.{ Account, WIPAccount }
import com.junabbott.services.TransactionService

object TransactionActor {
  final case class GetTransactions(accountId: Int) {
    require(accountId >= 0, "transaction account id must be greater than zero")
  }
  final case class CreateTransaction(accountId: Int, transactionType: String, amount: Double) {
    require(accountId >= 0, "transaction account id must be greater than zero")
    require(transactionType == "purchase", "transaction type must be purchase")
    require(amount > 0, "transaction amount must be greater than zero")
  }
  final case class Transaction(id: Int, transactionType: String, createDatetime: ZonedDateTime, amount: Double)
  final case class CreateTransactionResp(result: String)
  def props(transactionService: TransactionService): Props = Props(new TransactionActor(transactionService))
}

class TransactionActor(transactionService: TransactionService) extends Actor with ActorLogging {
  import TransactionActor._
  implicit val ec = context.dispatcher

  def receive: Receive = {
    case CreateTransaction(accountId, transactionType, amount) =>
      val count = if (transactionType == "purchase") {
        transactionService.addPurchaseTransaction(accountId, amount)
      }
      val res = count match {
        case i: Int if i > 0 => "success"
        case _ => "fail"
      }
      sender ! CreateTransactionResp(res)

    case WIPAccount(replyTo, account) =>
      val transactions = transactionService.getTransactions(account.id)
      val principalOpt = transactionService.getPrincipal(account.id)
      replyTo ! Account(account.id, principalOpt.getOrElse(0), transactions)

    case "ping" =>
      sender() ! "online"
  }
}
