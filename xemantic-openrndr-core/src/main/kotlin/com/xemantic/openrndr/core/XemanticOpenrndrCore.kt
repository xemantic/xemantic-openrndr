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
  INSTAGRAM_4_5(1080, 1350)
}

enum class PreviewLocation {
  LEFT,
  BELOW
}

class ScreenOutput(
    private val buffer: ColorBuffer,
    override var enabled: Boolean = true
) : Extension {
  override fun afterDraw(drawer: Drawer, program: Program) {
    drawer.image(buffer, 0.0, 0.0, program.width.toDouble(), program.height.toDouble())
  }
}

open class FragmentShader(
    path: String,
    context: XemanticContext,
    width: Int = context.width,
    height: Int = context.height,
    shaderUrl: String = context.getShaderUrl(path)
) : Filter(
    if (context.production)  filterShaderFromUrl(shaderUrl)  else null,
    if (!context.production) filterWatcherFromUrl(shaderUrl) else null
) {
  private var resolution: Vector2 by parameters
  var time: Double by parameters
  init {
    resolution = Vector2(width.toDouble(), height.toDouble())
  }
}
