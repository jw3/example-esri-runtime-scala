package com.github.jw3.geo

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Stash, Timers}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.esri.arcgisruntime.geometry.{Point, SpatialReferences}
import com.esri.arcgisruntime.mapping.view.{Graphic, GraphicsOverlay}
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.github.jw3.geo.GraphicActor.MoveTo

import scala.concurrent.duration.DurationInt
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

  def gid(id: String): String = s"g$id"
}

class OverlayActor(o: GraphicsOverlay) extends Actor {
  def receive: Receive = {
    case TextMessage.Strict(encoded) ⇒
      val split = encoded.split(":")
      val id = split(0)
      val x = split(1).toDouble
      val y = split(2).toDouble

      graphicOf(id) ! GraphicActor.MoveTo(x, y)
  }

  def graphicOf(id: String): ActorRef = {
    context.child(OverlayActor.gid(id)) match {
      case Some(ref) ⇒ ref
      case None ⇒ context.actorOf(GraphicActor.props(id, o), OverlayActor.gid(id))
    }
  }
}

object GraphicActor {
  def props(id: String, o: GraphicsOverlay) = Props(new GraphicActor(id, o))

  val defaultMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, -0x00100, 10f)
  val defaultInactiveMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.X, -0x10000, 8f)

  case object Inactivity

  case class MoveTo(x: Double, y: Double)

}

class GraphicActor(id: String, o: GraphicsOverlay) extends Actor with Timers with Stash with ActorLogging {
  private val sr = SpatialReferences.getWgs84

  def unlocated: Receive = {
    case MoveTo(x, y) ⇒
      val pt = new Point(x, y, sr)
      val g = new Graphic(pt, GraphicActor.defaultMarker)

      o.getGraphics.add(g)
      context.become(active(g))
  }

  def active(g: Graphic): Receive = {
    unstashAll()

    {
      case MoveTo(x, y) ⇒
        timers.startSingleTimer(id, GraphicActor.Inactivity, 30.seconds)
        g.setGeometry(new Point(x, y, sr))

      case GraphicActor.Inactivity ⇒
        context.become(inactive(g))
    }
  }

  def inactive(g: Graphic): Receive = {
    val s = g.getSymbol
    g.setSymbol(GraphicActor.defaultInactiveMarker)

    {
      case MoveTo(_, _) ⇒
        stash()
        g.setSymbol(s)
        context.become(active(g))
    }
  }

  def receive: Receive = unlocated
}
