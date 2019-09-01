extra["openrndrVersion"] = "0.3.35"
extra["slf4jVersion"] = "1.7.26"
extra["logbackVersion"] = "1.2.3"
extra["kotlinLoggingVersion"] = "1.7.2"

plugins {
  id("nebula.kotlin") version "1.3.50" apply false
  id("nebula.maven-publish") version "9.0.2" apply false
  id("nebula.source-jar") version "9.0.2" apply false
}

allprojects {
  group = "com.xemantic.openrndr"
  version = "1.0-SNAPSHOT"

  repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/openrndr/openrndr")
  }

  apply(plugin = "nebula.maven-publish")
}

subprojects {
  apply(plugin = "nebula.source-jar")
  apply(plugin = "nebula.kotlin")

  configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
  }
}
