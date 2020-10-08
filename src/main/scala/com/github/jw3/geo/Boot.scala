package com.github.jw3.geo

import java.awt.Color

import com.esri.arcgisruntime.data.{Feature, GeoPackage}
import com.esri.arcgisruntime.internal.jni.CoreSimpleRenderer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.view.{Graphic, MapView}
import com.esri.arcgisruntime.mapping.{ArcGISMap, Basemap}
import com.esri.arcgisruntime.symbology.{Renderer, SimpleFillSymbol, SimpleLineSymbol, SimpleRenderer}
import com.esri.arcgisruntime.{ArcGISRuntimeEnvironment, symbology}
import com.github.jw3.geo.Label.{ExpressionInfo, Font, LabelPlacement}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage

import scala.collection.JavaConverters._

object Boot extends App {
  val config = ConfigFactory.load()
  val DefaultLocationSDK = config.getString("arcgis.runtime.sdk.path")
  ArcGISRuntimeEnvironment.setInstallDirectory(DefaultLocationSDK)

  Application.launch(classOf[DisplayMapSample], args: _*)
}

class DisplayMapSample extends Application with LazyLogging {
  var mapView: Option[MapView] = None

  def start(stage: Stage): Unit = {
    if(getParameters.getUnnamed.isEmpty)
      throw new Error("usage: boot <base-gpkg>")

    val stackPane = new StackPane
    val scene = new Scene(stackPane)
    stage.setTitle("example map")
    stage.setWidth(1024)
    stage.setHeight(768)
    stage.setScene(scene)
    stage.show()

    val gpkg = new GeoPackage(getParameters.getUnnamed.get(0))
    gpkg.loadAsync()

    // todo;;
    Thread.sleep(2000)

    class Xr extends Renderer(new CoreSimpleRenderer) {
      val s = new SimpleRenderer()
      def getSymbol(feature: Feature): symbology.Symbol =
        s.getSymbol(feature)

      def getSymbol(graphic: Graphic): symbology.Symbol =
        s.getSymbol(graphic)
    }

    val roadRenderer = new SimpleRenderer(
      new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF000000, 3)
    )

    val woodlandRenderer = new SimpleRenderer(
      new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xAA5dd272, new SimpleLineSymbol())
    )

    val terrainRenderer = new SimpleRenderer(
      new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xAA808080, 1)
    )

    val fieldRenderer = new SimpleRenderer(
      new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFFff4500, new SimpleLineSymbol())
    )

    val basefeatures = gpkg.getGeoPackageFeatureTables.asScala.map(new FeatureLayer(_)).sortBy(_.getName)
    basefeatures.foreach{ fl =>fl.getName.toLowerCase.split("-").last match {
      case "roads" =>
        fl.setScaleSymbols(true)
        fl.setRenderer(roadRenderer)
        fl.setLabelsEnabled(true)
        fl.getLabelDefinitions.add(Label.LabelDefinition(
          ExpressionInfo("$feature.full_stree"),
          LabelPlacement.Point.CenterStart,
          Label.Symbol(Color.black, Font(16, "bold"))
        ))
      case "landcover" => fl.setRenderer(woodlandRenderer)
      case "terrain" => fl.setRenderer(terrainRenderer)
      case "fields" => fl.setRenderer(fieldRenderer)
    }}

    val base = basefeatures.foldLeft(new Basemap()) { (base, l) =>
      base.getBaseLayers.add(l)
      base
    }

    val mv = {
      val v = new MapView
      val map = new ArcGISMap(base)
      map.setReferenceScale(20000)
      v.setMap(map)
      // v.addMapScaleChangedListener(s => println(s.getSource.getMapScale))
      v
    }
    stackPane.getChildren.addAll(mv)

//    val wsuri = s"$wsreq/watch/device"
//    logger.info("connecting layer to {}", wsuri)
//
//    val tl = new GraphicsOverlay()
//    Streaming.connect(tl, wsuri)
//    mv.getGraphicsOverlays.add(tl)

    mapView = Some(mv)
  }

  override def stop(): Unit = mapView.foreach(_.dispose)

  def wsreq(cfg: Config): String = {
    val host = cfg.getString("geo.http.host")
    val port = cfg.getInt("geo.http.port")
    s"ws://$host:$port/api"
  }
}
