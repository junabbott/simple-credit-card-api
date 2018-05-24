package com.junabbott.actors

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestKit, TestProbe }
import com.junabbott.actors.AccountActor._
import com.junabbott.services.AccountService
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

class AccountActorTest extends TestKit(ActorSystem("AccountActorTest")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll with MockFactory {

  val accountServiceMock = mock[AccountService]
  val transactionActorProbe = TestProbe("transactionActorProbe")
  val accountActor = system.actorOf(Props(classOf[AccountActor], transactionActorProbe.ref, accountServiceMock))

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "AccountActor" should {
    "be able to create an account" in {
      (accountServiceMock.addAccount _).expects().returning(0)
      accountActor ! CreateAccount
      expectMsg(NewAccount(0))
    }
    "be able to get an account" in {
      accountActor ! GetAccount(1)
      transactionActorProbe.expectMsg(WIPAccount(self, Account(1, 0, Nil)))
    }
    "be able to handle ping" in {
      accountActor ! "ping"
      expectMsg("online")
    }
  }

}
