/*
 * xemantic-openrndr - a playground for OPENRNDR extensions
 * Copyright (C) 2020  Kazimierz Pogoda
 *
 * This file is part of xemantic-openrndr.
 *
 * xemantic-openrndr  is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * xemantic-openrndr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with xemantic-openrndr.
 * If not, see <https://www.gnu.org/licenses/>.
 */

@Suppress("MayBeConstant")
object V {
  val kotlin = "1.3.72"
  val kotlinLogging = "1.7.9"
  val openrndr = "0.3.43"
  val orx = "0.3.52"
  val slf4j = "1.7.30"
  val junit = "5.6.2"
  val atrium = "0.12.0"
  val log4j = "2.13.3"
  val jackson = "2.11.0"
  val springDependencyManagement = "1.0.6.RELEASE"
}

plugins {
  `maven-publish`
  kotlin("jvm") version "1.3.72" apply false
  id("io.spring.dependency-management") version "1.0.9.RELEASE"
  id("org.jetbrains.dokka") version "0.10.1"
}

allprojects {
  repositories {
    jcenter()
    maven(url = "https://dl.bintray.com/openrndr/openrndr")
  }
}

subprojects {

  group = "com.xemantic.openrndr"
  version = "1.0-SNAPSHOT"

  apply {
    plugin("io.spring.dependency-management")
    plugin("kotlin")
    plugin("maven-publish")
    plugin("org.jetbrains.dokka")
  }

  configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
  }

  dependencyManagement {
    dependencies {

      // implementation dependencies
      dependency("org.jetbrains.kotlin:kotlin-reflect:${V.kotlin}")
      dependency("io.github.microutils:kotlin-logging:${V.kotlinLogging}")
      dependency("org.openrndr:openrndr-core:${V.openrndr}")
      dependency("org.openrndr:openrndr-ffmpeg:${V.openrndr}")
      dependency("org.openrndr.extra:orx-midi:${V.orx}")

      // test dependencies
      dependency("org.jetbrains.kotlin:kotlin-test-junit5:${V.kotlin}")
      dependency("org.junit.jupiter:junit-jupiter-api:${V.junit}")
      dependency("org.junit.jupiter:junit-jupiter:${V.junit}")
      dependency("ch.tutteli.atrium:atrium-fluent-en_GB:${V.atrium}")
      dependency("ch.tutteli.atrium:atrium-api-fluent-en_GB-kotlin_1_3:${V.atrium}")
      dependency("org.apache.logging.log4j:log4j-api:${V.log4j}")
      dependency("org.apache.logging.log4j:log4j-core:${V.log4j}")
      dependency("org.apache.logging.log4j:log4j-slf4j-impl:${V.log4j}")
      dependency("com.fasterxml.jackson.core:jackson-databind:${V.jackson}")
      dependency("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${V.jackson}")
    }
  }

  dependencies {
    "testImplementation"("org.junit.jupiter:junit-jupiter-api")
    "testImplementation"("ch.tutteli.atrium:atrium-fluent-en_GB")
    "testImplementation"("ch.tutteli.atrium:atrium-api-fluent-en_GB-kotlin_1_3")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter")
    "testRuntimeOnly"("org.apache.logging.log4j:log4j-api")
    "testRuntimeOnly"("org.apache.logging.log4j:log4j-core")
    "testRuntimeOnly"("org.apache.logging.log4j:log4j-slf4j-impl")
    "testRuntimeOnly"("com.fasterxml.jackson.core:jackson-databind")
    "testRuntimeOnly"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  }

  tasks.dokka {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
  }

  val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier.set("javadoc")
    from(tasks.dokka)
  }

  publishing {
    publications {
      create<MavenPublication>("default") {
        from(components["java"])
        artifact(dokkaJar)
      }
    }
  }

}


//allprojects {
//
//
//  repositories {
//    mavenCentral()
//    maven(url = "https://dl.bintray.com/openrndr/openrndr")
//  }
//
//  apply(plugin = "nebula.maven-publish")
//}

//subprojects {
//  apply(plugin = "nebula.source-jar")
//  apply(plugin = "nebula.kotlin")
//
//  configure<JavaPluginConvention> {
//    sourceCompatibility = JavaVersion.VERSION_1_8
//  }
//}
