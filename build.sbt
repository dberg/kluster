lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.cybergstudio.kluster",
      scalaVersion := "2.12.3",
      version      := Process("git rev-parse HEAD").lines.head,
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
        "-Ywarn-unused-import"
      )
    )),
    name := "kluster",
    libraryDependencies ++= {
      val akkaV = "2.5.3"
      Seq(
        "com.lightbend.akka" %% "akka-management-cluster-http" % "0.3",
        "com.typesafe.akka" %% "akka-cluster" % akkaV,
        "com.typesafe.akka" %% "akka-cluster-metrics" % akkaV
      )
    }
  )

scalacOptions in (Compile, console) --= Seq("-Xfatal-warnings", "-Ywarn-unused-import")
scalacOptions in (Test, console) --= Seq("-Xfatal-warnings", "-Ywarn-unused-import")

test in assembly := {}

target in assembly := file("builddir")

cleanFiles += baseDirectory { base => base / "builddir" }.value

assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
