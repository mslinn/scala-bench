
val sharedSettings = Seq(
  //scalaVersion := "2.12.8"
  scalaVersion := "2.13.0"
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
      "com.lihaoyi"  %  "ammonite_2.13.0" % "1.6.9"
    ),
    javaOptions in run += ("-javaagent:" + (packageBin in (agent, Compile)).value)
)
