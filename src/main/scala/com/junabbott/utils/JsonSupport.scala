package com.junabbott.utils

import java.time.ZonedDateTime

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.junabbott.actors.AccountActor.{ Account, NewAccount }
import com.junabbott.actors.TransactionActor.{ CreateTransaction, CreateTransactionResp, Transaction }
import spray.json.{ DefaultJsonProtocol, JsString, JsValue, RootJsonFormat, deserializationError }

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object ZonedDateTimeFormat extends RootJsonFormat[ZonedDateTime] {
    def write(obj: ZonedDateTime): JsValue = JsString(obj.toString)

    def read(json: JsValue): ZonedDateTime = json match {
      case JsString(s) => ZonedDateTime.parse(s)
      case _ => deserializationError(json.toString())
    }
  }

  final case class Health(upTime: Long, accountSystem: String, transactionSystem: String)

  implicit val healthJsonFormat = jsonFormat3(Health)
  implicit val transactionJsonFormat = jsonFormat4(Transaction)
  implicit val newAccountJsonFormat = jsonFormat1(NewAccount)
  implicit val accountJsonFormat = jsonFormat3(Account)
  implicit val createTransactionJsonFormat = jsonFormat3(CreateTransaction)
  implicit val createTransactionResJsonFormat = jsonFormat1(CreateTransactionResp)
}
