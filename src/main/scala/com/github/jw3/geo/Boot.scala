package com.github.jw3.geo

import akka.actor.ActorSystem
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.view.{GraphicsOverlay, MapView}
import com.esri.arcgisruntime.mapping.{ArcGISMap, Basemap}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage

object Boot extends App {
  val config = ConfigFactory.load()
  val DefaultLocationSDK = config.getString("arcgis.runtime.sdk.path")
  ArcGISRuntimeEnvironment.setInstallDirectory(DefaultLocationSDK)

  Application.launch(classOf[DisplayMapSample], args: _*)
}

class DisplayMapSample extends Application with LazyLogging {
  implicit val system = ActorSystem()
  var mapView: Option[MapView] = None

  def start(stage: Stage): Unit = {
    val stackPane = new StackPane
    val scene = new Scene(stackPane)
    stage.setTitle("example map")
    stage.setWidth(1024)
    stage.setHeight(768)
    stage.setScene(scene)
    stage.show()

    val mv = new MapView
    mv.setMap(new ArcGISMap(Basemap.createImagery))
    stackPane.getChildren.addAll(mv)

    val wsuri = s"$wsreq/watch/device"
    logger.info("connecting layer to {}", wsuri)

    val tl = new GraphicsOverlay()
    Streaming.connect(tl, wsuri)
    mv.getGraphicsOverlays.add(tl)

    mapView = Some(mv)
  }

  override def stop(): Unit = mapView.foreach(_.dispose)

  def wsreq: String = {
    val cfg = system.settings.config
    val host = cfg.getString("geo.http.host")
    val port = cfg.getInt("geo.http.port")
    s"ws://$host:$port/api"
  }
}
