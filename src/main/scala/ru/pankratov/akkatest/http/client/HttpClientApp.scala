package ru.pankratov.akkatest.http.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.Future
import scala.util.{ Failure, Success }
import java.io.PrintWriter

object HttpClientApp extends App {
	val host = "akka.io"
	val port = 80
//	val host = "pankratov"
//	val port = 8888

	
	implicit val system = ActorSystem()
	implicit val materializer = ActorMaterializer()
	implicit val executionContext = system.dispatcher
	val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
		Http().outgoingConnection(host, port)
	val responseFuture: Future[HttpResponse] =
		Source.single(HttpRequest(uri = "/"))
			.via(connectionFlow)
			.runWith(Sink.head)
	responseFuture.andThen {
		case Success(_) => println("request succeded")
		case Failure(_) => println("request failed")
	}.andThen {
		case _ => system.terminate()
	}

}
