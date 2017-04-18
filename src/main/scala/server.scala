package org.strllar.ngst

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.typesafe.config.ConfigFactory
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.execution.deferred.DeferredResolver
import sangria.parser.QueryParser
import sangria.renderer.SchemaRenderer
import spray.json.{JsObject, JsString, JsValue}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

import slick.jdbc.PostgresProfile.api.Database

object Server extends App with SprayJsonSupport{
  implicit val system = ActorSystem("sangria-server")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.server.Route
  import akka.http.scaladsl.model.StatusCodes
  import akka.http.scaladsl.server.Directives._
  import sangria.marshalling.sprayJson._

  val appconfig = ConfigFactory.load().getConfig("ngst")
  val coredb = Database.forConfig("coredb", appconfig)

  val route: Route =
    (post & path("graphql")) {
      entity(as[JsValue]) { requestJson ⇒
        graphQLEndpoint(requestJson)
      }
    } ~
      get {
        getFromResource("graphiql.html")
      }

  def graphQLEndpoint(requestJson: JsValue) = {

    val JsObject(fields) = requestJson

    val JsString(query) = fields("query")

    val operation = fields.get("operationName") collect {
      case JsString(op) ⇒ op
    }

    val vars = fields.get("variables") match {
      case Some(obj: JsObject) ⇒ obj
      case _ ⇒ JsObject.empty
    }

    QueryParser.parse(query) match {

      // query parsed successfully, time to execute it!
      case Success(queryAst) ⇒
        complete(Executor.execute(SchemaDefinition.StellarSchema, queryAst, new LedgerHistory(coredb),
          variables = vars,
          operationName = operation)
          .map(StatusCodes.OK → _)
          .recover {
            case error: QueryAnalysisError ⇒ StatusCodes.BadRequest → error.resolveError
            case error: ErrorWithResolver ⇒ StatusCodes.InternalServerError → error.resolveError
          })

      // can't parse GraphQL query, return error
      case Failure(error) ⇒
        complete(StatusCodes.BadRequest, JsObject("error" → JsString(error.getMessage)))
    }
  }

  Http().bindAndHandle(route, "0.0.0.0", 8080)

  println(SchemaRenderer.renderSchema(SchemaDefinition.StellarSchema))
  scala.io.StdIn.readLine("Press Enter to Exit: \n")
  println("quit confirmed")

  val termsig = Future.sequence(Seq(
    Future.apply(coredb.close()),
    system.terminate()
  ))
  Await.result(termsig, Duration.Inf)
  println("Done")
}