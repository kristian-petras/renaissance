lazy val renaissanceCore = RootProject(uri("../../renaissance-core"))

lazy val dbShootout = (project in file("."))
  .settings(
    name := "dbshootout",
    organization := "org.renaissance",
    version := "0.1.0",
    scalafmtConfig := Some(file(".scalafmt.conf")),
    scalaVersion := "2.11.8",
    libraryDependencies ++= Seq(
      "org.openjdk.jmh" % "jmh-core" % "1.13",
      "com.github.jnr" % "jnr-posix" % "3.0.29",
      "org.apache.commons" % "commons-math3" % "3.6.1",
      "org.agrona" % "agrona" % "0.9.7",
      "org.lmdbjava" % "lmdbjava" % "0.0.2",
      "net.openhft" % "zero-allocation-hashing" % "0.6",
      "org.deephacks.lmdbjni" % "lmdbjni-linux64" % "0.4.6",
      "org.deephacks.lmdbjni" % "lmdbjni" % "0.4.6",
      "org.fusesource.leveldbjni" % "leveldbjni-linux64" % "1.8",
      "org.fusesource.leveldbjni" % "leveldbjni" % "1.8",
      "org.iq80.leveldb" % "leveldb-api" % "0.7",
      "org.mapdb" % "mapdb" % "3.0.1",
      "com.h2database" % "h2-mvstore" % "1.4.192",
      "org.rocksdb" % "rocksdbjni" % "4.8.0",
      "net.openhft" % "chronicle-core" % "2.17.2",
      "net.openhft" % "chronicle-bytes" % "2.17.7" exclude("net.openhft", "chronicle-core"),
      "net.openhft" % "chronicle-threads" % "2.17.1" exclude("net.openhft", "chronicle-core"),
      "net.openhft" % "chronicle-map" % "3.17.0" excludeAll(
        ExclusionRule("net.openhft", "chronicle-core"),
        ExclusionRule("net.openhft", "chronicle-bytes"),
        ExclusionRule("net.openhft", "chronicle-threads")
      ),
      "org.lwjgl" % "lwjgl" % "3.1.0",
      "org.lwjgl" % "lwjgl-lmdb" % "3.1.0",
      "org.jetbrains.xodus" % "xodus-environment" % "1.0.2",
      "org.jetbrains.xodus" % "xodus-openAPI" % "1.0.2"
    )
  )
  .dependsOn(
    renaissanceCore
  )
