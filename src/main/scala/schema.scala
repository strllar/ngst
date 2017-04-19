package org.strllar.ngst

/**
  * Created by ck on 2017/4/16.
  */

import akka.actor.Status.Success
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._
import slick.jdbc.H2Profile.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global //TODO: be careful

import evidences.StellarCoreDB

class StellarTome(val coredb :Database) {
  import StellarCoreDB.{LedgerHeader, ledgerheaders}

  def getLCL :Future[LedgerHeader] = {
    val lcl = coredb.run(
      ledgerheaders.sortBy(_.ledgerseq.desc.nullsLast).take(1).result
    )
    lcl.map(_.head)
  }

  def getLedger(lseq: Int): Future[Option[LedgerHeader]] = {
    coredb.run({
      ledgerheaders.filter(_.ledgerseq === lseq).result
    }).map(_.headOption)
  }
}


object SchemaDefinition {

  val LedgerHeader = ObjectType(
    "LedgerHeader",
    "The most brief and essential info of a ledger",

    fields[StellarTome, StellarCoreDB.LedgerHeader](

      Field("ledgerhash", StringType,
        description = Some("The hash of this ledger."),
        resolve = _.value.ledgerhash),

      Field("ledgerseq", IntType,
        description = Some("The seq of this ledger."),
        resolve = _.value.ledgerseq),

      Field("closetime", LongType,
        description = Some("The closetime of this ledger."),
        resolve = _.value.closetime),

      Field("data", StringType,
        description = Some("The data of this ledger."),
        resolve = _.value.data)
    )
  )

  val Ledger = //Abstract Ledger includes all infomation, not only LedgerHeader
    ObjectType(
      "Ledger",
      "The Ledger is the highest level structure representing the state of a ledger, cryptographically linked to previous ledgers.",
      fields[StellarTome, StellarCoreDB.LedgerHeader](
        Field("header", LedgerHeader,
          description = Some("The header of this ledger."),
          resolve = _.value),

        Field("closeAt", StringType,
          description = Some("The close time in text of this ledger."),
          resolve = (x) => {
            val time = java.time.Instant.ofEpochSecond(x.value.closetime)
            time.toString
          })
      )
    )
  val LEDGERSEQ = Argument("ledgerSeq", IntType, description = "sequence number of this ledger")

  val Query = ObjectType(
    "Query", fields[StellarTome, Unit](

      Field("lcl", Ledger,
        description = Some("Get Last Closed Ledger"),
        resolve = ctx ⇒ ctx.ctx.getLCL),

      Field("ledger", OptionType(Ledger),
        description = Some("Get Ledger by seq"),
        arguments = LEDGERSEQ :: Nil,
        resolve = ctx ⇒ ctx.ctx.getLedger(ctx arg LEDGERSEQ))

    ))

  val StellarSchema = Schema(Query)
}
