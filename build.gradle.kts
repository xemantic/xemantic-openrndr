allprojects {
  group = "com.xemantic.openrndr"
  version = "1.0-SNAPSHOT"
}

subprojects {
  repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/openrndr/openrndr")
  }
}
