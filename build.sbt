name := "ayumi"

lazy val commonSettings = Seq(
  organization := "shoma.me",
  version := "0.1.0",
  scalaVersion := "2.12.2"
)

lazy val `ayumi` = (project in file("."))
  .enablePlugins(PlayScala)
  .aggregate(model)
  .dependsOn(model)
  .settings(
    commonSettings
  )

lazy val model = (project in file("app/model"))
  .settings(commonSettings)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"

resolvers += Resolver.jcenterRepo

val silhouetteVer = "5.0.0"

libraryDependencies ++= Seq(
  ehcache ,
  ws ,
  guice,
  "com.typesafe.play"       %% "play-slick"                       % "3.0.0",
  "com.typesafe.play"       %% "play-slick-evolutions"            % "3.0.0",
  "com.typesafe.play"       %% "play-mailer"                      % "6.0.1",
  "com.typesafe.play"       %% "play-mailer-guice"                % "6.0.1",
  "com.rometools"           %  "rome"                             % "1.7.4",
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