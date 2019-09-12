
val sharedSettings = Seq(
  //scalaVersion := "2.12.10"
  scalaVersion := "2.13.0",
  scalacOptions ++= List(
    "-deprecation",
    "-feature",
    "-target:jvm-1.8",
    "-unchecked",
    "-Ywarn-numeric-widen",
    "-Xlint"
  )
)
val agent = project
  .settings(sharedSettings: _*)
  .settings(
    packageOptions in (Compile, packageBin) +=
      Package.ManifestAttributes( "Premain-Class" -> "agent.Agent" )
  )

val bench = project
  .dependsOn(agent)
  .settings(sharedSettings: _*)
  .settings(
    fork in run := true,

    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0" withSources(),
      "com.lihaoyi"             %  "ammonite_2.13.0"           % "1.6.9",
      "org.apache.poi"          %  "poi"                       % "4.1.0",
    ),
    javaOptions in run += ("-javaagent:" + (packageBin in (agent, Compile)).value)
)

ThisBuild / turbo := true
