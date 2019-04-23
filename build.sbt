import sbt.ExclusionRule
import sbtassembly.AssemblyPlugin.autoImport.ShadeRule

/*
 * Copyright 2016 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

lazy val commonSettings = Seq(
  name := "spark-bigquery",
  organization := "com.spotify",
  scalaVersion := "2.11.11",
  crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.8")
)

spName := "spotify/spark-bigquery"
sparkVersion := "2.4.0"
sparkComponents := Seq("core", "sql")
spAppendScalaVersion := false
spIncludeMaven := true
spIgnoreProvided := true
credentials += Credentials(Path.userHome / ".ivy2" / ".sbtcredentials")
parallelExecution in Test := false

lazy val shaded = (project in file("."))
  .settings(commonSettings)

libraryDependencies ++= Seq(
  "com.databricks" %% "spark-avro" % "4.0.0",
  "com.google.cloud.bigdataoss" % "bigquery-connector" % "hadoop2-0.13.13"
    exclude ("com.google.guava", "guava-jdk5"),
  "org.slf4j" % "slf4j-simple" % "1.7.21",
  "joda-time" % "joda-time" % "2.9.3",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
).map(_.excludeAll(
  ExclusionRule(organization = "log4j"),
  ExclusionRule(organization = "org.slf4j", name = "slf4j-log4j12")
))

assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("com.google.common.**" -> "repackaged.com.google.common.@1").inAll
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case x => MergeStrategy.first
}

addArtifact(artifact in (Compile, assembly), assembly)
crossPaths := false // Do not append Scala versions to the generated artifacts
autoScalaLibrary := false // This forbids including Scala related libraries into the dependency
skip in publish := true

// Release settings
licenses += "Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0")
releaseCrossBuild             := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
