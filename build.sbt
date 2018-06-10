name := "example-esri-runtime-scala"
organization := "com.github.jw3"

version := "0.1"
scalaVersion := "2.12.6"

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

libraryDependencies ++= Seq(
  "com.esri.arcgisruntime" % "arcgis-java" % "100.2.1",
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
