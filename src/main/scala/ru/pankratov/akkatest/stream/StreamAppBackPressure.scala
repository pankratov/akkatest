package ru.pankratov.akkatest.stream

import akka.stream._
import akka.stream.scaladsl._

import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths
import java.io.File

object StreamAppBackPressure  {

  def lineSink(filename: String): Sink[String, Future[IOResult]] = {
    Flow[String]
      .alsoTo(Sink.foreach(s => println(s"$filename: $s")))
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toFile(new File(filename)))(Keep.right)
  }

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("streamapp")
    implicit val materializer = ActorMaterializer()

    val source: Source[Int, NotUsed] = Source(1 to 100)
    val factorials: Source[BigInt, NotUsed] = 
      source.scan(BigInt(1))((acc, next) => acc * next)
    val sink1 = lineSink("factorial1.txt")
    val sink2 = lineSink("factorial2.txt")
    val slowSink2 = Flow[String].via(Flow[String]
      .throttle(1, 1.second, 1, ThrottleMode.shaping))
      .toMat(sink2)(Keep.right)
    val bufferedSink2 = Flow[String].buffer(5, OverflowStrategy.fail)
      .via(Flow[String]
      .throttle(1, 1.second, 1, ThrottleMode.shaping)
      .map { x =>  
      			(if (x.toInt < 1000) x else x.toInt/0).toString
     	})
      .toMat(sink2)(Keep.right)

    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val bcast = b.add(Broadcast[String](2))
      factorials.map(_.toString) ~> bcast.in
      bcast.out(0) ~> sink1
      bcast.out(1) ~> bufferedSink2
      ClosedShape
    })

    g.run()
  }
}
