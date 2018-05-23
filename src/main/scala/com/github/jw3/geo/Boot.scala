package com.github.jw3.geo

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.{ArcGISMap, Basemap}
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage

object Boot extends App {
  val DefaultLocationSDK = "/usr/local/arcgis-runtime-sdk-java"
  ArcGISRuntimeEnvironment.setInstallDirectory(DefaultLocationSDK)

  Application.launch(classOf[DisplayMapSample], args: _*)
}

class DisplayMapSample extends Application {
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

    mapView = Some(mv)
  }

  override def stop(): Unit = mapView.foreach(_.dispose)
}
