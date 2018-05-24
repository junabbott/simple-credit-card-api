package com.junabbott

import scala.concurrent.{ Await, ExecutionContextExecutor }
import scala.concurrent.duration._
import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.junabbott.actors.{ AccountActor, TransactionActor }
import com.junabbott.routes.APIRoutes
import com.junabbott.services.{ AccountService, TransactionService }
import scalikejdbc.config.DBs

object SimpleCreditCardServer extends App with APIRoutes {

  implicit val system: ActorSystem = ActorSystem("simpleCreditCardSystem")
  implicit def executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  DBs.setupAll()

  lazy val log = Logging(system, getClass)

  val transactionActor: ActorRef = system.actorOf(TransactionActor.props(new TransactionService), "transactionActor")
  val accountActor: ActorRef = system.actorOf(AccountActor.props(transactionActor, new AccountService), "accountActor")

  lazy val routes: Route = apiRoutes

  Http().bindAndHandle(routes, "0.0.0.0", 8080)

  log.debug(s"Server online at http://0.0.0.0:8080/")

  scala.sys.addShutdownHook {
    log.debug("Shutting down SimpleCreditCard Server")
    Await.result(system.whenTerminated, 30 seconds)
    DBs.closeAll()
  }
}
