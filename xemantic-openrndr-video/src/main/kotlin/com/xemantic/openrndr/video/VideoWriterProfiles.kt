package com.xemantic.openrndr.video

import org.openrndr.ffmpeg.VideoWriterProfile

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
