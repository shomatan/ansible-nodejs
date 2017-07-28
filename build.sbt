name := "play_cms"
 
version := "1.0" 
      
lazy val `play_cms` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

val silhouetteVer = "5.0.0-RC2"

libraryDependencies ++= Seq(
  ehcache ,
  ws ,
  guice,
  "com.typesafe.play"       %% "play-slick"                       % "3.0.0",
  "com.typesafe.play"       %% "play-slick-evolutions"            % "3.0.0",
  "org.postgresql"          %  "postgresql"                       % "42.1.1",
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

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += Resolver.jcenterRepo

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"