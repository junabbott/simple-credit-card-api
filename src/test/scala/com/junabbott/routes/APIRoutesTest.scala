package com.junabbott.routes

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, MessageEntity, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import com.junabbott.actors.AccountActor.{ Account, CreateAccount, GetAccount, NewAccount }
import com.junabbott.actors.TransactionActor.{ CreateTransaction, CreateTransactionResp }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

class APIRoutesTest extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures with APIRoutes {

  val transactionActorProbe = TestProbe("transactionActorProbe")
  val accountActorProbe = TestProbe("accountActorProbe")
  override val transactionActor: ActorRef = transactionActorProbe.ref
  override val accountActor: ActorRef = accountActorProbe.ref

  "APIRoutes" should {
    "be able to check application health" in {
      val result = Get("/health") ~> apiRoutes ~> runRoute
      accountActorProbe.expectMsg("ping")
      accountActorProbe.reply("online")
      transactionActorProbe.expectMsg("ping")
      transactionActorProbe.reply("online")

      check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val health = entityAs[Health]
        health.accountSystem shouldEqual "online"
        health.transactionSystem shouldEqual "online"
      }(result)
    }
    "be able to create an account" in {
      val result = Post("/accounts") ~> apiRoutes ~> runRoute

      accountActorProbe.expectMsg(CreateAccount)
      accountActorProbe.reply(NewAccount(1))

      check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        entityAs[NewAccount].id shouldEqual 1
      }(result)
    }
    "be able to get an account" in {
      val result = Get("/accounts/1") ~> apiRoutes ~> runRoute

      accountActorProbe.expectMsg(GetAccount(1))
      accountActorProbe.reply(Account(1, 0, Nil))

      check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        entityAs[Account].id shouldEqual 1
      }(result)
    }
    "not able to get an acount with invalid account id" in {
      Get("/accounts/") -> apiRoutes -> check {
        status shouldEqual StatusCodes.BadRequest
      }
      Get("accounts/test") -> apiRoutes -> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }
    "be able to add purchase transaction" in {
      val createTransaction = CreateTransaction(1, "purchase", 50)
      val entity = Marshal(createTransaction).to[MessageEntity].futureValue
      val result = Post("/transactions").withEntity(entity) ~> apiRoutes ~> runRoute

      transactionActorProbe.expectMsg(createTransaction)
      transactionActorProbe.reply(CreateTransactionResp("success"))

      check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        entityAs[CreateTransactionResp].result shouldEqual "success"
      }(result)
    }
    "not be able to add other transaction" in {
      val entity = HttpEntity.apply(ContentTypes.`application/json`, """{ "accountId": 76, "transactionType": "payment", "amount": 100}""")
      Post("/transactions", entity) ~> apiRoutes ~> check {
        rejection
      }
    }
  }
}
