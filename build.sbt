name := "example-esri-runtime-scala"

version := "0.1"

scalaVersion := "2.12.6"

resolvers += Resolver.bintrayRepo("esri", "arcgis")

libraryDependencies ++= Seq(
  "com.esri.arcgisruntime" % "arcgis-java" % "100.2.1"
)