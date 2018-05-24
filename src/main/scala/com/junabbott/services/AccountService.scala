package com.junabbott.services

import scalikejdbc._

class AccountService(dbName: Symbol = 'default) {
  def addAccount(): Long = {
    NamedDB(dbName) localTx { implicit session =>
      sql"insert into account (id, create_datetime) values (default, default)".updateAndReturnGeneratedKey.apply()
    }
  }
}
