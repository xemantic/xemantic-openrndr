dependencies {
  implementation(kotlin("stdlib-jdk8"))
  compile(project(":xemantic-openrndr-core"))
  compile("org.openrndr:openrndr-ffmpeg:${rootProject.extra["openrndrVersion"]}")
}
