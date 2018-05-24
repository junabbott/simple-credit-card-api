package com.junabbott.actors

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestKit }
import com.junabbott.actors.AccountActor.{ Account, WIPAccount }
import com.junabbott.actors.TransactionActor.{ CreateTransaction, CreateTransactionResp }
import com.junabbott.services.TransactionService
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

class TransactionActorTest extends TestKit(ActorSystem("TransactionActorTest")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll with MockFactory {

  val transactionServiceMock = mock[TransactionService]
  val transactionActor = system.actorOf(Props(classOf[TransactionActor], transactionServiceMock), "transactionActor")

  override def beforeAll: Unit = {
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "TransactionActor" should {
    "be able to create a transaction" in {
      (transactionServiceMock.addPurchaseTransaction _).expects(0, 50).returning(1)
      val createTransaction = CreateTransaction(0, "purchase", 50)
      transactionActor ! createTransaction
      expectMsg(CreateTransactionResp("success"))
    }
    "be able to process WIPAccount" in {
      (transactionServiceMock.getTransactions _).expects(0).returning(Nil)
      (transactionServiceMock.getPrincipal _).expects(0).returning(Some(0))
      transactionActor ! WIPAccount(self, Account(0, 0, Nil))
      expectMsg(Account(0, 0, Nil))
    }
    "be able to handle ping" in {
      transactionActor ! "ping"
      expectMsg("online")
    }
  }
}
