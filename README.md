# What is NGST?

A [GraphQL](http://graphql.org) server to [Stellar](http://stellar.org) network.

Maybe a Html5 dashboard SPA will be included in the near future.

# How to setup?
 
## hack
 ```sh
# get `sbt` from http://www.scala-sbt.org/ installed

$git clone https://github.com/strllar/ngst.git
$cd ngst

#setup bridges to stellar-core database (TODO)
#...

$sbt run
```

Then open http://127.0.0.1:8080/ in browser

## quickstart
Open http://q.stellar.org in browser.

# Usecases

## Get the latest leger
```

```
## Get the ledger was processed in specific timestamp
## What is the top N richest address 
## Count trusters of an asset
## How will the next inflation be distributed?

## Multi Query Operations with fragment 
```
query {

  ahahah :ledger(ledgerSeq :0) {
    ...all
  }
  
  bhahah :lcl {
    ...all
  }
}
fragment all on Ledger {
    seq
}
```
## More
Happy hacking for your applications or [learn more about GraphQL](http://graphql.org/learn/)

If you have any question, feel free to create an [issue](https://github.com/strllar/ngst/issues)
