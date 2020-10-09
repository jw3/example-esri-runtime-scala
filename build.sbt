name := "example-esri-runtime-scala"
organization := "com.github.jw3"

version := "0.1"
scalaVersion := "2.12.12"

resolvers += Resolver.bintrayRepo("esri", "arcgis")

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-Ywarn-unused-import",
  //"-Xfatal-warnings",
  "-Xlint:_",
  //
  // resolve apparent proguard collision
  "-Yresolve-term-conflict:object"
)

val arcgisVersion = "100.9.0"
libraryDependencies ++= Seq(
  // misc
  "com.iheart" %% "ficus" % "1.4.3",
  "com.esri.arcgisruntime" % "arcgis-java" % arcgisVersion,
  "com.esri.arcgisruntime" % "arcgis-java-jnilibs" % arcgisVersion,
  "com.esri.arcgisruntime" % "arcgis-java-resources" % arcgisVersion,
  "com.github.jw3" %% "geotrellis-vector" % "12.2.0.0",
  "com.github.jw3" %% "geotrellis-slick" % "12.2.0.0",
  "org.julienrf" % "play-json-derived-codecs_2.12" % "4.0.0",
  // akka
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",
  // logging
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.12",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
)
