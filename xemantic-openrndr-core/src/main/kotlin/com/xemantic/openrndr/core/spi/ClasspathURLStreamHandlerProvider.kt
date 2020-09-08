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
