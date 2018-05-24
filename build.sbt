import sbt.dsl.enablePlugins

lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion    = "2.5.12"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.junabbott",
      scalaVersion    := "2.12.5",
      version         := "1.0.0"
    )),
    name := "simple-credit-card-api",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalikejdbc" %% "scalikejdbc" % "3.2.2",
      "org.scalikejdbc" %% "scalikejdbc-config" % "3.2.2",
      "mysql" % "mysql-connector-java" % "8.0.11",

      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "com.h2database" % "h2" % "1.4.197" % Test,
      "org.scalamock" %% "scalamock" % "4.1.0" % Test
    )
  )

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)