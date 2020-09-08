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

package com.xemantic.openrndr.core

import mu.KotlinLogging
import org.openrndr.Configuration
import org.openrndr.Extension
import org.openrndr.Fullscreen
import org.openrndr.Program
import org.openrndr.draw.*
import org.openrndr.math.IntVector2
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
    val frameRate: Int = 60, // it should be aligned with screen frame rate to have consistent results
    val customWidth: Int = -1,
    val customHeight: Int = -1
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

  fun configure(config: Configuration) {
    when (format) {
      Format.CUSTOM -> {
        width  = customWidth
        height = customHeight
      }
      else -> {
        width  = format.width
        height = format.height
      }
    }
    when (format) {
      Format.FULLSCREEN -> { config.fullscreen = Fullscreen.CURRENT_DISPLAY_MODE }
      else -> {
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
      width: Int = this.width,
      height: Int = this.height,
      format: ColorFormat = ColorFormat.RGB,
      type: ColorType = ColorType.FLOAT16,
      multisample: BufferMultisample = BufferMultisample.Disabled
  ): ColorBuffer {
    return org.openrndr.draw.colorBuffer(
        width = width,
        height = height,
        contentScale = contentScale,
        format = format,
        type = type,
        multisample = multisample
    )
  }

  fun fragmentShader(
      path: String,
      width: Int = this.width,
      height: Int = this.height,
      doubleBuffering: Boolean = true,
      colorFormat: ColorFormat = ColorFormat.RGB
  ): FragmentShader {
    val shader = FragmentShader(
        path,
        this,
        width = width,
        height = height,
        doubleBuffering = doubleBuffering,
        colorFormat = colorFormat)
    fragmentShaders.add(shader)
    return shader
  }

  fun fragmentShader(path: String, doubleBuffering: Boolean = true, colorBuffer: ColorBuffer): FragmentShader{
    val shader = FragmentShader(path, this, doubleBuffering = doubleBuffering, colorBuffer = colorBuffer)
    fragmentShaders.add(shader)
    return shader
  }

  val resolution get() = IntVector2(width, height).vector2

  val screenRatio get() = width.toDouble() / height.toDouble()

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
    for (shader in fragmentShaders) {
      shader.time = time
      shader.swapBuffers()
    }



    currentFrame++

  }

}
