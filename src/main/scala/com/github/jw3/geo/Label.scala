package com.github.jw3.geo

import java.awt.Color

import julienrf.json.derived
import play.api.libs.json._

object Label {
  case class LabelDefinition(labelExpressionInfo: ExpressionInfo, labelPlacement: LabelPlacement, symbol: Symbol)
  object LabelDefinition {
    implicit val format: Writes[LabelDefinition] = o ⇒
      JsObject(
        Map(
          "labelExpressionInfo" → ExpressionInfo.format.writes(o.labelExpressionInfo),
          "labelPlacement" → LabelPlacement.format.writes(o.labelPlacement),
          "symbol" → Symbol.format.writes(o.symbol)
        )
    )

    implicit def toEsriLabelDef(l: Label.LabelDefinition): com.esri.arcgisruntime.arcgisservices.LabelDefinition = {
      com.esri.arcgisruntime.arcgisservices.LabelDefinition.fromJson(Json.toJson(l).toString())
    }
  }

  case class ExpressionInfo(expression: String)
  object ExpressionInfo {
    implicit val format: Format[ExpressionInfo] = derived.oformat()
  }

  case class Font(size: Int, weight: String)
  object Font {
    implicit val format: Format[Font] = derived.oformat()
  }

  case class Symbol(color: Color, font: Font)
  object Symbol {
    implicit val format: Writes[Symbol] = o ⇒
      JsObject(
        Map("color" → AwtColor.writes(o.color), "font" → Font.format.writes(o.font), "type" → JsString("esriTS"))
    )
  }

  sealed trait LabelPlacement {
    def prefix: String = "esriServer"
    def separator: String
    def suffix: String = getClass.getTypeName.split("""\$""").last

    override def toString: String = s"$prefix$separator$suffix"
  }

  object LabelPlacement {
    object Point {
      sealed trait Placement extends LabelPlacement { def separator: String = "PointLabelPlacement" }
      case object AboveCenter extends Placement
      case object BelowCenter extends Placement
      case object CenterStart extends Placement
      case object CenterCenter extends Placement
      case object AboveLeft extends Placement
      case object BelowLeft extends Placement
      case object CenterLeft extends Placement
      case object AboveRight extends Placement
      // ...
    }
    object Line {
      sealed trait Placement extends LabelPlacement { def separator: String = "LinePlacement" }
      case object AboveAfter extends Placement
      case object AboveStart extends Placement
      case object BelowAfter extends Placement
      case object BelowStart extends Placement
      case object CenterAfter extends Placement
      case object CenterStart extends Placement
      // ...
    }
    object Polygon {
      sealed trait Placement extends LabelPlacement { def separator: String = "PolygonPlacement" }
      case object AlwaysHorizontal extends Placement
    }

    implicit val format: Writes[LabelPlacement] = o ⇒ JsString(o.toString)
  }

  object AwtColor extends Writes[Color] {
    def writes(o: Color): JsValue = {
      JsArray(Seq(JsNumber(o.getRed), JsNumber(o.getGreen), JsNumber(o.getBlue), JsNumber(o.getAlpha)))
    }
  }
}
