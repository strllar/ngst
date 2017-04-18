package org.strllar.ngst

/**
  * Created by ck on 2017/4/16.
  */

import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

import slick.jdbc.H2Profile.api._
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global //TODO: be careful

case class LedgerHeader(seq :Int)

class LedgerHistory(val coredb :Database) {
  def getLCL :Future[LedgerHeader] = {
    val lcl = coredb.run(
      evidences.StellarCoreDB.ledgerheaders.map(_.ledgerseq).max.result
    )
    lcl.map({
      case Some(x) => LedgerHeader(x)
      case None => LedgerHeader(0)
    })
  }
  def getLedger(lseq: Int): Option[LedgerHeader] = Some(LedgerHeader(lseq))
}


object SchemaDefinition {
  val Ledger = //Abstract Ledger includes all infomation, not only LedgerHeader
    ObjectType(
      "Ledger",
      "The Ledger is the highest level structure representing the state of a ledger, cryptographically linked to previous ledgers.",
      fields[LedgerHistory, LedgerHeader](
        Field("seq", IntType,
          Some("The seq of this ledger."),
          tags = ProjectionName("_id") :: Nil,
          resolve = _.value.seq)
      )
    )
  val LEDGERSEQ = Argument("ledgerSeq", IntType, description = "sequence number of this ledger")

  val Query = ObjectType(
    "Query", fields[LedgerHistory, Unit](

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
