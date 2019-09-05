# xemantic-openrndr

[OPENRNDR](https://openrndr.org) extensions which are too specific for my workflow to be
contributed back to the project. It's also a sandbox for ideas which still need
some time to evolve before they are ready, but might be already useful for someone
else.

## using the project

In the project root type:
```
$ ./gradelw publishToMavenLocal
```

And in your `build.grade.kts` add:
```kotlin
val xemanticOpenrndrVersion = "1.0-SNAPSHOT"

dependencies {
  // ...
  compile("com.xemantic.openrndr:xemantic-openrndr-core:$xemanticOpenrndrVersion")
  compile("com.xemantic.openrndr:xemantic-openrndr-color:$xemanticOpenrndrVersion")
  compile("com.xemantic.openrndr:xemantic-openrndr-video:$xemanticOpenrndrVersion")
  // ...
}
```

Pick modules you need.

## modules

### core

Motivation: I wanted to make some generative videos and have matching outcome
on the screen and in the mp4 file. Therefore I introduced double measuring of time,
which is either based on real time or on frame and frame rate. This led to
a bit of standardization of my shader code having 2 common uniforms:

* `vec2 resolution`
* `float time` (in seconds)

It is also possible to differentiate screen output resolution and video resolution.
For example I had to generate videos in FullHD portrait format and the screen was
just a preview.

Example usage:

```kotlin
fun main() = application {
  val context = XemanticContext(
      format = Format.FULL_HD_PORTRAIT,
      previewLocation = PreviewLocation.LEFT, // useful for live coding
      duration = 20.0 // in seconds
  )
  configure { context.configure(this) }
  program {
    extend(context)
    val sceneBuffer = context.colorBuffer()
    val shader = context.fragmentShader("MyShader.frag")
    extend {
      // feedback loop
      shader.apply(arrayOf(sceneBuffer), sceneBuffer)
    }
    extend(ScreenOutput(sceneBuffer))
  }
}
```

If you are working with shaders in IDE they will be recompiled on every change
for convenient development, but
if the application is packaged into jar file to be used in production, shader
files will come from the classpath.
To achieve this seamlessly the `core` module installs `classpath` protocol handler
and `XemanticContext` will detect if it is running from IDE, or from packaged
jar.

The `XemanticContext` will also configure more advanced logging with logback which
means you can configure the logging and also redirect it to a file.

### video

```kotlin
    extend(ColorBufferRecorder(
        sceneBuffer,
        enabled = true,
        preset = ColorBufferRecorder.Preset.INSTAGRAM
    ))
```

It will be recorded under the file name which contains your `main()` method,
in the local `video` folder. The video file name will also contain
extra information about format, preset and recording time. 

You can have more than one `ColorBufferRecorder` for different purposes.
For example it was useful for me to also dump raw Kinect input while recording
generated visuals:
 
```kotlin
    extend(ColorBufferRecorder(
        kinectBuffer,
        enabled = true,
        suffix = "kinect",
        preset = ColorBufferRecorder.Preset.REAL_TIME
    ))
    extend(ColorBufferRecorder(
        sceneBuffer,
        enabled = true,
        preset = ColorBufferRecorder.Preset.REAL_TIME
    ))
```

Presets:

* `REAL_TIME` - crf 10 and ultrafast compression
* `HIGH_QUALITY` - crf 10 should reduce blocky gradients 
* `INSTAGRAM` - optimized for size and frame rate, maybe it will make it less likely
for Instagram to transcode the video into lower quality
* `DEMO_FESTIVAL` - high quality but 25 FPS - see https://demofestival.com

### color

This module is the first one to be moved to the [orx](https://github.com/openrndr/orx) project
(OPENRNDR extensions), at the moment it contains only kotlin+OPENRNDR reimplementation
of [Alan Zucconi](https://www.alanzucconi.com)'s spectralZucconi6 - natural light
dispersion spectrum mapped to RGB values.
A [shader version](https://github.com/openrndr/orx/blob/master/orx-kinect-common/src/main/resources/org/openrndr/extra/kinect/depth-to-colors-zucconi6.frag)
is already in the `orx` project as a filter to be used with Kinect support
and it seems that both of them can be moved to new `orx-color` module soon.
