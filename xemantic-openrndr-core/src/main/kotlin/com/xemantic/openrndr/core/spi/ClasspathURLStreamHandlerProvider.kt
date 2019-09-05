package com.xemantic.openrndr.core.spi

import com.xemantic.openrndr.core.XemanticContext
import java.net.URLStreamHandler
import java.net.spi.URLStreamHandlerProvider
import java.io.IOException
import java.net.URL
import java.net.URLConnection

class ClasspathURLStreamHandlerProvider : URLStreamHandlerProvider() {
  override fun createURLStreamHandler(protocol: String?): URLStreamHandler? {
    return if ("classpath" == protocol) {
      object : URLStreamHandler() {
        @Throws(IOException::class)
        override fun openConnection(u: URL): URLConnection? {
          val resource =
              XemanticContext::class.java.classLoader.getResource(u.path)
                  ?: throw IOException("Could not find resource: $u")
          return resource.openConnection()
        }
      }
    } else null
  }
}
