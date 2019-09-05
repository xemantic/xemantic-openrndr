package com.xemantic.openrndr.core

import mu.KotlinLogging
import org.openrndr.Configuration
import org.openrndr.Extension
import org.openrndr.Fullscreen
import org.openrndr.Program
import org.openrndr.draw.*
import org.openrndr.math.IntVector2
import org.slf4j.bridge.SLF4JBridgeHandler
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.ArrayList

class XemanticContext(
    val format: Format = Format.FULLSCREEN,
    val previewScale :Double =
        when (format) {
          Format.FULL_HD_PORTRAIT -> .56
          Format.INSTAGRAM_4_5 -> .78
          else -> 1.0
        },
    val duration: Double = -1.0, // non-stop (in seconds)
    val previewLocation: PreviewLocation = PreviewLocation.BELOW,
    val glslPath: String = "src/main/glsl",
    val mediaPath: String = "media",
    val frameRate: Int = 60 // it should be aligned with screen frame rate to have consistent results
) : Extension {

  override var enabled: Boolean = true

  var realTime: Boolean = true // might be changed by ColorBufferRecorder

  private val logger = KotlinLogging.logger {}

  var width: Int = format.width
    private set
  var height: Int = format.height
    private set

  private val nonStop: Boolean = (duration == -1.0)

  var currentFrame: Long = 0

  var time: Double = 0.0

  val mainClass = Class.forName(
      Thread.currentThread().stackTrace.find{e -> e.methodName == "main"}?.className
  )
  val production = (mainClass.`package`.implementationVersion != null)

  private val fragmentShaders = ArrayList<FragmentShader>()

  init {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
    Logger.getLogger("").level = Level.FINEST
  }

  fun configure(config: Configuration) {
    if (format == Format.FULLSCREEN) {
      config.fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    } else {
      config.width  = (width.toDouble() * previewScale).toInt()
      config.height = (height.toDouble() * previewScale).toInt()
      config.hideWindowDecorations = true
      config.position =
          if (previewLocation == PreviewLocation.LEFT)
            IntVector2(0, 0)
          else
            IntVector2(0, Format.FULL_HD.height)
    }
  }

  private fun getDurationInfo(): String {
    return if (nonStop) "non-stop"
    else "duration: $duration sec, $frameRate FPS"
  }

  override fun setup(program: Program) {
    if (format == Format.FULLSCREEN) {
      width  = program.width
      height = program.height
    }
    logger.info {
      "XemanticContext $format[${width}x$height] ${getDurationInfo()}"
    }
  }

  fun getMediaUrl(path: String): String {
    return if (production) "classpath:$mediaPath/$path"
    else "file:$mediaPath/$path"
  }

  fun getShaderUrl(path: String): String {
    return if (production) "classpath:glsl/$path"
    else "file:$glslPath/$path"
  }

  fun colorBuffer(
      contentScale: Double = 1.0,
      format: ColorFormat = ColorFormat.RGB,
      type: ColorType = ColorType.FLOAT16,
      multisample: BufferMultisample = BufferMultisample.Disabled
  ): ColorBuffer {
    return colorBuffer(
        width = width,
        height = height,
        contentScale = contentScale,
        format = format,
        type = type,
        multisample = multisample
    )
  }

  fun fragmentShader(path: String): FragmentShader {
    val shader = FragmentShader(path, this)
    fragmentShaders.add(shader)
    return shader
  }

  private fun updateTime(program: Program) {
    time = if (realTime) program.seconds
        else (currentFrame.toDouble() / frameRate.toDouble())
  }

  override fun beforeDraw(drawer: Drawer, program: Program) {
    updateTime(program)
    for (shader in fragmentShaders) {
      shader.time = time
    }
  }

  override fun afterDraw(drawer: Drawer, program: Program) {
    if (!nonStop && (time >= duration)) {
      logger.info { "finishing after $duration seconds"}
      program.application.exit()
    }
    currentFrame++
  }

}
