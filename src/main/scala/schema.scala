package org.strllar.ngst

/**
  * Created by ck on 2017/4/16.
  */

import sangria.schema._

import scala.concurrent.ExecutionContext.Implicits.global //TODO: be careful

object SchemaDefinition {

  val IAccountingSubject = InterfaceType(
    "IAccountingSubject",
    "Accounting subject in ledger",
    () => fields[StellarTome, StellarTome.AccountingSubject](
      Field("totalAmount", LongType,
        description = Some("total amount of XLM"),
        resolve = _.value.totalAmount)
    )
  )

  val ILedgerEntry = InterfaceType(
    "LedgerEntry",
    "single entry in ledger",
    () => fields[StellarTome, StellarTome.LedgerEntry](
      //TODO: Change to Ledger using deferResolve
      Field("lastModifiedLedgerSeq", IntType,
        description = Some("ledger the LedgerEntry was last changed."),
        resolve = _.value.lastModifiedLedgerSeq)
    )
  )

  val OAccountEntry = ObjectType(
    "AccountEntry",
    "",
    interfaces[StellarTome, StellarTome.AccountFusion](ILedgerEntry),
    fields[StellarTome, StellarTome.AccountFusion](

    )
  )

  val OAccountRow = ObjectType(
    "AccountRow",
    "Row in table 'accounts'",
    fields[StellarTome, StellarTome.AccountFusion](
      Field("accountid", StringType,
        description = Some("as name"),
        resolve = _.value.solo.accountid),
      Field("balance", LongType,
        description = Some("as name"),
        resolve = _.value.solo.balance),
      Field("seqnum", LongType,
        description = Some("as name"),
        resolve = _.value.solo.seqnum),
      Field("numsubentries", IntType,
        description = Some("as name"),
        resolve = _.value.solo.numsubentries),
      Field("inflationdest", StringType,
        description = Some("as name"),
        resolve = _.value.solo.inflationdest),
      Field("homedomain", StringType,
        description = Some("as name"),
        resolve = _.value.solo.homedomain),
      Field("thresholds", StringType,
        description = Some("as name"),
        resolve = _.value.solo.thresholds),
      Field("flags", IntType,
        description = Some("as name"),
        resolve = _.value.solo.flags),
      Field("lastmodified", IntType,
        description = Some("as name"),
        resolve = _.value.solo.lastmodified)
    )
  )

  val OAccountFusion = ObjectType(
    "Account",
    "Account in stellar network",
    interfaces[StellarTome, StellarTome.AccountFusion](IAccountingSubject),
    () => fields[StellarTome, StellarTome.AccountFusion](
      Field("db", OAccountRow,
        description = Some("The basic info of this account.(from DB)"),
        resolve = _.value),
      Field("xdr", OAccountEntry,
        description = Some("The account entry in bucket.(from XDR)"),
        resolve = _.value),
      Field("birthLedger", OLedgerFusion,
        description = Some("The ledger in which this account created"),
        resolve =  _.ctx.getLCL.map(StellarTome.LedgerFusion))
    )
  )

  val OLedgerHeaderRow = ObjectType(
    "LedgerHeaderRow",
    "Row in table 'ledgerheaders'",

    fields[StellarTome, StellarTome.LedgerFusion](
      Field("ledgerhash", StringType,
        description = Some("as name"),
        resolve = _.value.header.ledgerhash),
      Field("prevhash", StringType,
        description = Some("as name"),
        resolve = _.value.header.prevhash),
      Field("bucketlisthash", StringType,
        description = Some("as name"),
        resolve = _.value.header.bucketlisthash),
      Field("ledgerseq", IntType,
        description = Some("as name"),
        resolve = _.value.header.ledgerseq),
      Field("closetime", LongType,
        description = Some("as name"),
        resolve = _.value.header.closetime),
      Field("data", StringType,
        description = Some("as name"),
        resolve = _.value.header.data)
    )
  )

  val OLedgerFusion = //Abstract Ledger includes all infomation, not only LedgerHeader
    ObjectType(
      "Ledger",
      "The Ledger is the highest level structure representing the state of a ledger, cryptographically linked to previous ledgers.",
      fields[StellarTome, StellarTome.LedgerFusion](
        Field("db", OptionType(OLedgerHeaderRow),
          description = Some("The header of this ledger.(from DB)"),
          resolve = _.value),

        Field("closeAt", StringType,
          description = Some("The close time in text of this ledger."),
          resolve = (x) => {
            val time = java.time.Instant.ofEpochSecond(x.value.header.closetime)
            time.toString
          })
      )
    )
  val LEDGERSEQ = Argument("ledgerSeq", IntType, description = "sequence number of this ledger")
  val ACCOUNTID = Argument("accountID", StringType, description = "as name")

  val Query = ObjectType(
    "Query",
    "Top Query Functions to Stellar Network",
    fields[StellarTome, Unit](
      Field("lcl", OLedgerFusion,
        description = Some("Get Last Closed Ledger"),
        resolve = ctx ⇒ ctx.ctx.getLCL.map(StellarTome.LedgerFusion)),

      Field("ledger", OptionType(OLedgerFusion),
        description = Some("Get Ledger by seq"),
        arguments = LEDGERSEQ :: Nil,
        resolve = ctx ⇒ ctx.ctx.getLedger(ctx arg LEDGERSEQ).map(_ map StellarTome.LedgerFusion)),

        Field("account", OptionType(OAccountFusion),
        description = Some("Get Account Information"),
        arguments = ACCOUNTID :: Nil,
        resolve = ctx ⇒ ctx.ctx.getAccount(ctx arg ACCOUNTID).map(_ map StellarTome.AccountFusion))

    ))

  val StellarSchema = Schema(Query)
}
