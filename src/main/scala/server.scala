package org.strllar.ngst

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.JsValue


object Server extends App with SprayJsonSupport{
  implicit val system = ActorSystem("sangria-server")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher
  import akka.http.scaladsl.server.Directives._

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
    complete("OK")
  }
  Http().bindAndHandle(route, "0.0.0.0", 8080)
}