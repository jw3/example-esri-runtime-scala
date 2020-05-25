package com.github.jw3.geo

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import geotrellis.vector.Geometry
import geotrellis.vector.io.json.Implicits._
import geotrellis.vector.io.json.{GeoJson, JsonFeatureCollection}
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Random, Success}

object Simulate extends App {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  val vector = args.headOption.getOrElse("/tmp/vector.geojson")
  println(s"loading $vector")

  Source
    .fromIterator(
      () ⇒
        GeoJson
          .fromFile[JsonFeatureCollection](vector)
          .getAllMultiLines()
          .iterator
    )
    .map(ml ⇒ id → ml)
    .flatMapMerge(
      10, { t ⇒
        Source
          .fromIterator(() ⇒ t._2.lines.flatMap(_.points).iterator)
          .map(pt ⇒ HookCall("sim", s"${pt.y}:${pt.x}", t._1, "now"))
          .throttle(Random.nextInt(1) + 1, FiniteDuration(Random.nextInt(1), TimeUnit.SECONDS))
      }
    )
    .runWith(Sink.foreach(println))

  def id() = UUID.randomUUID.toString.take(8)
  def add(id: String): Future[HttpResponse] = {
    val f = Http().singleRequest(HttpRequest(HttpMethods.POST, s"http://localhost:9000/api/device/$id"))
    f.onComplete {
      case Success(_) ⇒ println(id)
      case Failure(ex) ⇒
        println("create failed, " + ex.getMessage)
    }
    f
  }
  def move(): Sink[HookCall, _] = {
    Sink.foreach[HookCall] { call ⇒
      //'{coreid: env.ID, event: env.EVENT, published_at: "now", data: env.POS}'
      Http()
        .singleRequest(
          HttpRequest(
            HttpMethods.POST,
            s"http://localhost:9000/api/device/${call.coreid}/move",
            entity = HttpEntity(ContentTypes.`application/json`, call.toJson.compactPrint)
          )
        )
        .onComplete {
          case Success(_) ⇒ println(call)
          case Failure(ex) ⇒
            println("move failed, " + ex.getMessage)
        }
    }
  }
}

case class F(`type`: String, properties: Map[String, String], geometry: Geometry)
object F {
  implicit val format: RootJsonFormat[F] = jsonFormat3(F.apply)
}

// copied from the example server, should be published instead
final case class HookCall(event: String, data: String, coreid: String, published_at: String)
object HookCall extends DefaultJsonProtocol {
  implicit val format: RootJsonFormat[HookCall] = jsonFormat4(HookCall.apply)
}
