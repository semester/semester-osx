
lazy val buildSettings = Seq(
  version := "0.14.6",
  organization := "org.watermint",
  scalaVersion := "2.11.5",
  crossScalaVersions := Seq("2.11.5"),
  resolvers ++= Seq(
    "sonatype-public" at "https://oss.sonatype.org/content/groups/public",
    "atlassian-public" at "https://maven.atlassian.com/repository/public",
    "maven-central" at "http://repo1.maven.org/maven2",
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    Resolver.sonatypeRepo("snapshots")
  ),
  libraryDependencies ++= Seq(
    "org.specs2" %% "specs2-core" % "latest.release" % "test",
    "org.specs2" %% "specs2-junit" % "latest.release" % "test",
    "junit" % "junit" % "latest.release" % "test"
  ),
  ivyXML :=
    <dependencies>
      <exclude org="log4j" name="log4j" />
      <exclude org="commons-logging" name="commons-logging" />
      <exclude org="org.slf4j" name="slf4j-log4j12" />
    </dependencies>
)

lazy val serviceNsunc = project.in(file("semester-service-nsunc"))
  .dependsOn(readymadeCf)
  .settings(buildSettings: _*)

lazy val readymadeCf = project.in(file("semester-readymade-cf"))
  .settings(buildSettings: _*)

