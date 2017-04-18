package org.strllar.ngst.evidences

/**
  * Created by kring on 2017/4/18.
  */

import slick.jdbc.PostgresProfile.api._

object StellarCoreDB {
  class ledgerheaders(tag: Tag) extends Table[(String, String, String, Int, Long, String)](tag, "ledgerheaders") {
    def ledgerhash = column[String]("ledgerhash")
    def prevhash = column[String]("prevhash")
    def bucketlisthash = column[String]("bucketlisthash")
    def ledgerseq = column[Int]("ledgerseq")
    def closetime = column[Long]("closetime")
    def data = column[String]("data")
    def * = (ledgerhash, prevhash, bucketlisthash, ledgerseq, closetime, data)
  }

  class accounts(tag: Tag) extends Table[(String, Long, Long, Int, String, String, String, Int, Int)](tag, "accounts") {
    def accountid = column[String]("accountid")
    def balance = column[Long]("balance")
    def seqnum = column[Long]("seqnum")
    def numsubentries = column[Int]("numsubentries")
    def inflationdest = column[String]("inflationdest")
    def homedomain = column[String]("homedomain")
    def thresholds = column[String]("thresholds")
    def flags = column[Int]("flags")
    def lastmodified = column[Int]("lastmodified")

    def * = (accountid, balance, seqnum, numsubentries, inflationdest, homedomain, thresholds, flags, lastmodified)
  }

  val ledgerheaders = TableQuery[ledgerheaders]
  val accounts = TableQuery[accounts]
}
