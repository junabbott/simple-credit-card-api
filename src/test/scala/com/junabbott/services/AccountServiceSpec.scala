package com.junabbott.services

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }
import scalikejdbc.{ DB, _ }
import scalikejdbc.config.{ DBs, DBsWithEnv }

class AccountServiceSpec extends WordSpec with Matchers with BeforeAndAfterAll {

  val accountService = new AccountService('account)

  override def beforeAll: Unit = {
    //    DBsWithEnv("accountService").setupAll()
    DBs.setup('account)
    NamedDB('account) localTx { implicit session =>
      sql"CREATE TABLE IF NOT EXISTS account(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,create_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)".update().apply()
    }
  }

  override def afterAll: Unit = {
    //DBsWithEnv("accountService").closeAll()
    DBs.close('account)
  }

  "AccountService" should {
    "be able to add an account" in {
      accountService.addAccount() shouldEqual 1
    }
  }
}
