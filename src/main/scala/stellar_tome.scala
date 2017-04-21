package org.strllar.ngst

import org.strllar.ngst.evidences.StellarCoreDB
import org.strllar.ngst.evidences.StellarCoreDB.{LedgerHeader, ledgerheaders}

import scala.concurrent.Future

import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global //TODO: be careful

/**
  * Created by kring on 2017/4/21.
  */
object StellarTome {
  //types for shallow and acyclic entities restricted by scala type system.
  trait AccountingSubject {
    def totalAmount :Long
  }

  trait LedgerEntry {
    def lastModifiedLedgerSeq :Int
  }

  case class AccountFusion(val solo :StellarCoreDB.Account) extends LedgerEntry with AccountingSubject {
    override def lastModifiedLedgerSeq = solo.lastmodified

    override def totalAmount: Long = solo.balance
  }

  case class LedgerFusion(val header :StellarCoreDB.LedgerHeader) {

  }
}

class StellarTome(val coredb :Database) {
  import StellarCoreDB.{LedgerHeader, ledgerheaders, Account, accounts}

  def getLCL :Future[LedgerHeader] = {
    val lcl = coredb.run(
      ledgerheaders.sortBy(_.ledgerseq.desc.nullsLast).take(1).result
    )
    lcl.map(_.head)
  }

  def getLedger(lseq :Int): Future[Option[LedgerHeader]] = {
    coredb.run({
      ledgerheaders.filter(_.ledgerseq === lseq).result
    }).map(_.headOption)
  }

  def getAccount(acctid :String) :Future[Option[Account]] = {
    coredb.run({
      accounts.filter(_.accountid === acctid).result
    }).map(_.headOption)
  }
}