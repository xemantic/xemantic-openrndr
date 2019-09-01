dependencies {
  implementation(kotlin("stdlib-jdk8"))
  compile("org.openrndr:openrndr-core:${rootProject.extra["openrndrVersion"]}")
  compile("org.slf4j:slf4j-api:${rootProject.extra["slf4jVersion"]}")
  compile("io.github.microutils:kotlin-logging:${rootProject.extra["kotlinLoggingVersion"]}")
  runtime("org.slf4j:jul-to-slf4j:${rootProject.extra["slf4jVersion"]}")
  runtime("ch.qos.logback:logback-classic:${rootProject.extra["logbackVersion"]}")
}
