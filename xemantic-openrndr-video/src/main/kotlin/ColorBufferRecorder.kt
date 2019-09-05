package com.xemantic.openrndr.video

import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.*
import org.openrndr.ffmpeg.VideoWriter
import org.openrndr.ffmpeg.VideoWriterProfile
import java.io.File
import java.time.LocalDateTime

import com.xemantic.openrndr.core.XemanticContext
import mu.KotlinLogging
import java.lang.IllegalStateException

/**
 * Similar to ScreenRecorder, but will record the specified buffer instead
 * of the whole screen. The buffer might have different dimensions than the screen.
 *
 * @see org.openrndr.ffmpeg.ScreenRecorder
 * @param buffer the buffer to take video frames from.
 * @param suffix might be useful for simultaneous recording of many buffers
 *                of the same program.
 */
class ColorBufferRecorder(
    val buffer: ColorBuffer,
    override var enabled: Boolean = true,
    val preset: Preset = Preset.HIGH_QUALITY,
    val suffix: String? = null,
    val profile: VideoWriterProfile = when(preset) {
      Preset.REAL_TIME -> newRealTimeVideoWriterProfile()
      Preset.HIGH_QUALITY -> newHighQualityVideoWriterProfile()
      Preset.DEMO_FESTIVAL -> newHighQualityVideoWriterProfile(frameRate = 25.0)
      Preset.INSTAGRAM -> newInstagramVideoWriterProfile()
    },
    val inputFrameRate: Int = 60
) : Extension {

  enum class Preset {
    REAL_TIME,
    HIGH_QUALITY,
    DEMO_FESTIVAL,
    INSTAGRAM
  }

  private val logger = KotlinLogging.logger {}

  var frame: Int = 0

  private lateinit var videoWriter: VideoWriter
  private lateinit var videoTarget: RenderTarget

  override fun setup(program: Program) {
    if (!enabled) { return }
    val context: XemanticContext = program.extensions.find {
      extension -> extension::class == XemanticContext::class
    } as XemanticContext? ?: throw IllegalStateException(
        "XemanticContext must be registered as one of the extension"
    )
    context.realTime = (preset == Preset.REAL_TIME)

    videoTarget = renderTarget(buffer.width , buffer.height) {
      colorBuffer()
    }

    fun Int.z(zeroes: Int = 2): String {
      val sv = this.toString()
      var prefix = ""
      for (i in 0 until Math.max(zeroes - sv.length, 0)) {
        prefix += "0"
      }
      return "$prefix$sv"
    }

    val dt = LocalDateTime.now()
    val basename = context.mainClass.simpleName.removeSuffix("Kt")
    val ext = if (suffix != null) "-$suffix" else ""
    val spec = "${context.format.name}-${preset.name}"
    val filename = "video/$basename$ext-$spec-${dt.year.z(4)}-${dt.month.value.z()}-${dt.dayOfMonth.z()}-${dt.hour.z()}.${dt.minute.z()}.${dt.second.z()}.mp4"

    File(filename).parentFile.let {
      if (!it.exists()) {
        it.mkdirs()
      }
    }

    logger.info { "Recording: $filename" }

    videoWriter = VideoWriter()
        .profile(profile)
        .output(filename)
        .size(buffer.width, buffer.height)
        .frameRate(inputFrameRate)
        .start()
  }

  override fun afterDraw(drawer: Drawer, program: Program) {
    drawer.isolatedWithTarget(videoTarget) {
        ortho(videoTarget)
        image(buffer)
    }
    videoWriter.frame(videoTarget.colorBuffer(0))
    frame++
  }

}

fun newRealTimeVideoWriterProfile(
    crf: Int = 10, // 0 would mean lossless, 10 seems like a reasonable value
    // to prevent blocky artifacts on gradients, ffmpeg default is 23
    frameRate: Double = 60.0
) : VideoWriterProfile {
  return object : VideoWriterProfile() {
    override fun arguments(): Array<String> {
      return arrayOf(
          "-vcodec", "libx264",
          "-pix_fmt", "yuv420p",
          "-an",
          "-crf", "$crf",
          "-r", "$frameRate",
          "-preset", "ultrafast"
      )
    }
  }
}

fun newHighQualityVideoWriterProfile(
    crf: Int = 10, // 0 would mean lossless, 10 seems like a reasonable value
    // to prevent blocky artifacts on gradients, ffmpeg default is 23
    frameRate: Double = 60.0
) : VideoWriterProfile {
  return object : VideoWriterProfile() {
    override fun arguments(): Array<String> {
      return arrayOf(
          "-vcodec", "libx264",
          "-pix_fmt", "yuv420p",
          "-an",
          "-crf", "$crf",
          "-r", "$frameRate",
          "-preset", "slow",
          "-tune", "film",
          "-movflags", "+faststart"
      )
    }
  }
}

fun newInstagramVideoWriterProfile(
    crf: Int = 20, // seems that maybe it should go really up for instagram?
    frameRate: Double = 30000.0 / 1001.0
) : VideoWriterProfile {
  return object : VideoWriterProfile() {
    override fun arguments(): Array<String> {
      return arrayOf(
          "-vcodec", "libx264",
          "-pix_fmt", "yuv420p",
          "-an",
          "-crf", "$crf",
          "-r", "$frameRate",
          "-preset", "veryslow",
          "-tune", "film",
          "-level", "4.0",
          "-color_primaries", "1",
          "-color_trc", "1",
          "-colorspace", "1",
          "-movflags", "+faststart"
      )
    }
  }
}
