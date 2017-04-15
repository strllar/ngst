package org.strllar.ngst

/**
  * Created by ck on 2017/4/16.
  */

import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

import scala.concurrent.Future

case class LedgerHeader(seq :Int)

class LedgerHistory {
  def getLedger(lseq: Int): Option[LedgerHeader] = Some(LedgerHeader(0))
}


object SchemaDefinition {
  val LedgerHeader =
    ObjectType(
      "LedgerHeader",
      "The LedgerHeader is the highest level structure representing the state of a ledger, cryptographically linked to previous ledgers.",
      fields[LedgerHistory, LedgerHeader](
        Field("seq", IntType,
          Some("The seq of this ledger header."),
          tags = ProjectionName("_id") :: Nil,
          resolve = _.value.seq)
      )
    )
  val LEDGERSEQ = Argument("ledgerSeq", IntType, description = "sequence number of this ledger")

  val Query = ObjectType(
    "Query", fields[LedgerHistory, Unit](
      Field("ledger", OptionType(LedgerHeader),
        arguments = LEDGERSEQ :: Nil,
        resolve = ctx ⇒ ctx.ctx.getLedger(ctx arg LEDGERSEQ))
    ))

  val StellarSchema = Schema(Query)
}
