package org.strllar.ngst

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.execution.deferred.DeferredResolver
import sangria.parser.QueryParser
import spray.json.{JsObject, JsString, JsValue}

import scala.util.{Failure, Success}


object Server extends App with SprayJsonSupport{
  implicit val system = ActorSystem("sangria-server")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.server.Route
  import akka.http.scaladsl.model.StatusCodes
  import akka.http.scaladsl.server.Directives._
  import sangria.marshalling.sprayJson._

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
        complete(Executor.execute(SchemaDefinition.StellarSchema, queryAst, new LedgerHistory,
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
}