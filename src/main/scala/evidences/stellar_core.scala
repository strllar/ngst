package org.strllar.ngst.evidences

/**
  * Created by kring on 2017/4/18.
  */

import slick.jdbc.PostgresProfile.api._

object StellarCoreXDR {

}

object StellarCoreDB {

  case class LedgerHeader(ledgerhash :String, prevhash :String, bucketlisthash :String, ledgerseq :Int, closetime :Long, data :String)
  class ledgerheaders(tag: Tag) extends Table[LedgerHeader](tag, "ledgerheaders") {
    def ledgerhash = column[String]("ledgerhash")
    def prevhash = column[String]("prevhash")
    def bucketlisthash = column[String]("bucketlisthash")
    def ledgerseq = column[Int]("ledgerseq")
    def closetime = column[Long]("closetime")
    def data = column[String]("data")
    def * = (ledgerhash, prevhash, bucketlisthash, ledgerseq, closetime, data) <> (LedgerHeader.tupled, LedgerHeader.unapply)
  }

  case class Account(accountid :String, balance :Long, seqnum :Long, numsubentries :Int, inflationdest :String, homedomain :String, thresholds :String, flags :Int, lastmodified :Int)
  class accounts(tag: Tag) extends Table[Account](tag, "accounts") {
    def accountid = column[String]("accountid")
    def balance = column[Long]("balance")
    def seqnum = column[Long]("seqnum")
    def numsubentries = column[Int]("numsubentries")
    def inflationdest = column[String]("inflationdest")
    def homedomain = column[String]("homedomain")
    def thresholds = column[String]("thresholds")
    def flags = column[Int]("flags")
    def lastmodified = column[Int]("lastmodified")

    def * = (accountid, balance, seqnum, numsubentries, inflationdest, homedomain, thresholds, flags, lastmodified) <> (Account.tupled, Account.unapply)
  }

  val ledgerheaders = TableQuery[ledgerheaders]
  val accounts = TableQuery[accounts]
}
