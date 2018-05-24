package com.junabbott.routes

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.junabbott.actors.AccountActor._

import scala.concurrent.duration._
import akka.pattern.ask
import com.junabbott.actors.TransactionActor.{ CreateTransaction, CreateTransactionResp }
import com.junabbott.utils.JsonSupport

import scala.concurrent.ExecutionContextExecutor

trait APIRoutes extends JsonSupport {

  implicit def system: ActorSystem
  implicit def executor: ExecutionContextExecutor

  val accountActor: ActorRef
  val transactionActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)

  lazy val apiRoutes: Route =
    logRequestResult("akka-http-service") {
      path("health") {
        get {
          val res = for {
            accountActorRes <- (accountActor ? "ping").map(_.toString)
            transactionActorRes <- (transactionActor ? "ping").map(_.toString)
          } yield {
            Health(system.uptime, accountActorRes, transactionActorRes)
          }
          complete(res)
        }
      } ~
        pathPrefix("accounts") {
          post {
            complete {
              (accountActor ? CreateAccount).mapTo[NewAccount]
            }
          } ~
            path(IntNumber) { id =>
              get {
                complete {
                  (accountActor ? GetAccount(id)).mapTo[Account]
                }
              }
            }
        } ~
        path("transactions") {
          post {
            entity(as[CreateTransaction]) { transaction =>
              complete {
                (transactionActor ? transaction).mapTo[CreateTransactionResp]
              }
            }
          }
        }
    }
}
