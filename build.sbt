import Dependencies._

name := "ByteMe_sc"
version := "0.1"
scalaVersion := "2.13.5"

libraryDependencies ++= dependencies
scalacOptions ++= Seq("-Ymacro-annotations")
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full
)

testFrameworks += new TestFramework("weaver.framework.CatsEffect")
