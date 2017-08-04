name := "play_cms"

lazy val commonSettings = Seq(
  organization := "shoma.me",
  version := "0.1.0",
  scalaVersion := "2.12.2"
)

lazy val `play_cms` = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    commonSettings
  )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += Resolver.jcenterRepo

val silhouetteVer = "5.0.0-RC2"

libraryDependencies ++= Seq(
  ehcache ,
  ws ,
  guice,
  "com.typesafe.play"       %% "play-slick"                       % "3.0.0",
  "com.typesafe.play"       %% "play-slick-evolutions"            % "3.0.0",
  "org.postgresql"          %  "postgresql"                       % "42.1.1",
  "net.codingwell"          %% "scala-guice"                      % "4.1.0",
  "com.iheart"              %% "ficus"                            % "1.4.1",        // config lib, used by Silhouette,
  "com.mohiva"              %% "play-silhouette"                  % silhouetteVer,
  "com.mohiva"              %% "play-silhouette"                  % silhouetteVer,
  "com.mohiva"              %% "play-silhouette-password-bcrypt"  % silhouetteVer,
  "com.mohiva"              %% "play-silhouette-crypto-jca"       % silhouetteVer,
  "com.mohiva"              %% "play-silhouette-persistence"      % silhouetteVer,
  "com.mohiva"              %% "play-silhouette-testkit"          % silhouetteVer   % "test",
  "org.scalatestplus.play"  %% "scalatestplus-play"               % "3.1.1"         % Test,
  "org.slf4j"               %  "slf4j-nop"                        % "1.6.4",
  specs2 % Test
)

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"