package com.github.jw3.geo

import akka.Done
import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.mapping.view.{Graphic, GraphicsOverlay}
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol

import scala.collection.mutable
import scala.concurrent.Promise

object Streaming {
  def connect(o: GraphicsOverlay, to: String)(implicit sys: ActorSystem): Promise[Option[Message]] = {
    implicit val mat = ActorMaterializer()
    val overlay = sys.actorOf(OverlayActor.props(o))
    val source = Source.maybe[Message]
    val sink = Sink.actorRef(overlay, Done)
    val wsr = WebSocketRequest(to)

    val flow = Flow.fromSinkAndSourceMat(sink, source)(Keep.right)
    Http().singleWebSocketRequest(wsr, flow)._2
  }
}

object OverlayActor {
  def props(o: GraphicsOverlay) = Props(new OverlayActor(o))
}

class OverlayActor(o: GraphicsOverlay) extends Actor {
  private val locationGraphics = mutable.Map[String, Graphic]()
  private val locationMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, -0x10000, 10f)

  def receive: Receive = {
    case TextMessage.Strict(encoded) ⇒
      import com.esri.arcgisruntime.geometry.SpatialReferences
      val split = encoded.split(":")
      val id = split(0)
      val x = split(1).toDouble
      val y = split(2).toDouble
      val pt = new Point(x, y, SpatialReferences.getWgs84)

      if (!locationGraphics.contains(id)) {
        val g = new Graphic(pt, locationMarker)
        locationGraphics(id) = g
        o.getGraphics.add(g)
      }
      locationGraphics(id).setGeometry(pt)
  }
}
