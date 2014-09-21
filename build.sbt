name := "sample"

version := "1.0-SNAPSHOT"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.github.ReactiveMongo-Queries" %% "reactivemongo-queries" % "0.10.0.a-SNAPSHOT"
)     

play.Project.playScalaSettings
