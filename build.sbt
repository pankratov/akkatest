lazy val akkaDependencies = {
	val akkaV = "2.4.8"
	Seq(
		"com.typesafe.akka"   %%  "akka-actor"    % akkaV,
		"com.typesafe.akka"   %%  "akka-stream"    % akkaV,
		"com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test"
	)
}

lazy val commonSettings = Seq(
		organization := "ru.pankratov",
		name := "akkatest",
		version := "0.1",
		scalaVersion := "2.11.8"
	)


lazy val root = (project in file("."))
	.settings(commonSettings: _*)
	.settings(
		libraryDependencies ++= akkaDependencies
	)

