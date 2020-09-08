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

import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.*
import org.openrndr.math.Vector2

enum class Format(val width: Int, val height: Int) {
  FULLSCREEN(-1, -1),
  FULL_HD(1920, 1080),
  FULL_HD_PORTRAIT(1080, 1920),
  INSTAGRAM_SQUARE(1080, 1080),
  INSTAGRAM_4_5(1080, 1350),
  CUSTOM(-1, -1),
}

enum class PreviewLocation {
  LEFT,
  BELOW
}

class ScreenOutput(
    private val fragmentShader: FragmentShader,
    override var enabled: Boolean = true
) : Extension {
  override fun afterDraw(drawer: Drawer, program: Program) {
    drawer.image(fragmentShader.colorBuffer, 0.0, 0.0, program.width.toDouble(), program.height.toDouble())
  }
}



// TODO proper encapsulation of color buffers
open class FragmentShader(
    path: String,
    context: XemanticContext,
    shaderUrl: String = context.getShaderUrl(path),
    width: Int = context.width,
    height: Int = context.height,
    colorFormat: ColorFormat = ColorFormat.RGB,
    var colorBuffer: ColorBuffer = context.colorBuffer(width = width, height = height, format = colorFormat),
    val doubleBuffering: Boolean = true
) : Filter(
    if (context.production)  filterShaderFromUrl(shaderUrl)  else null,
    if (!context.production) filterWatcherFromUrl(shaderUrl) else null
) {
  var previousColorBuffer: ColorBuffer? = null
  init {
    if (doubleBuffering) {
      previousColorBuffer = colorBuffer(
          colorBuffer.width,
          colorBuffer.height,
          colorBuffer.contentScale,
          colorBuffer.format,
          colorBuffer.type
      )
    }
  }
  private var resolution: Vector2 by parameters
  var time: Double by parameters
  init {
    resolution = Vector2(width.toDouble(), height.toDouble())
  }
  fun swapBuffers() {
    val tempBuffer = colorBuffer
    if (doubleBuffering) {
      colorBuffer = previousColorBuffer!!
    }
    previousColorBuffer = tempBuffer
  }
  fun shade(source: Array<ColorBuffer>) {
    apply(source, colorBuffer)
  }
}
