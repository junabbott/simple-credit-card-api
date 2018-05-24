package com.junabbott.actors

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import com.junabbott.actors.TransactionActor.Transaction
import com.junabbott.services.AccountService

object AccountActor {
  final object CreateAccount
  final case class GetAccount(id: Int) {
    require(id > 0, "account id must be greater than zero")
  }
  final case class NewAccount(id: Int)
  final case class Account(id: Int, principal: Double, transactions: Seq[Transaction])
  final case class WIPAccount(replyTo: ActorRef, account: Account)
  def props(transactionActor: ActorRef, accountService: AccountService): Props = Props(new AccountActor(transactionActor, accountService))
}

class AccountActor(transactionActor: ActorRef, accountService: AccountService) extends Actor with ActorLogging {
  import AccountActor._
  implicit val ec = context.dispatcher

  def receive: Receive = {
    case CreateAccount =>
      val id = accountService.addAccount()
      sender ! NewAccount(id.toInt)

    case GetAccount(accountId) =>
      transactionActor ! WIPAccount(sender(), Account(accountId, 0, Nil))

    case "ping" =>
      sender ! "online"
  }
}
