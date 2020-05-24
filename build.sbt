import scala.sys.process._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.cybergstudio.kluster",
      scalaVersion := "2.13.2",
      version      := Process("git rev-parse HEAD").lineStream.head,
      scalacOptions := Seq(
        "-deprecation",
        "-encoding", "UTF-8",
        "-feature",
        "-unchecked",
        "-Xfatal-warnings",
        "-Xlint",
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen",
        "-Ywarn-value-discard",
        "-Ywarn-unused:imports"
      )
    )),
    name := "kluster",
    libraryDependencies ++= {
      val akkaV = "2.6.5"
      val akkaMngV = "1.0.7"
      Seq(
        "com.typesafe.akka" %% "akka-http" % "10.1.12",
        "com.typesafe.akka" %% "akka-stream" % akkaV,
        "com.lightbend.akka.management" %% "akka-management" % akkaMngV,
        "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaMngV,
        "com.typesafe.akka" %% "akka-cluster" % akkaV,
        "com.typesafe.akka" %% "akka-cluster-metrics" % akkaV,
        "ch.qos.logback" % "logback-classic" % "1.2.3",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
      )
    }
  )

scalacOptions in (Compile, console) --= Seq("-Xfatal-warnings", "-Ywarn-unused-import")
scalacOptions in (Test, console) --= Seq("-Xfatal-warnings", "-Ywarn-unused-import")

test in assembly := {}

target in assembly := file("builddir")

cleanFiles += baseDirectory { base => base / "builddir" }.value

assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
