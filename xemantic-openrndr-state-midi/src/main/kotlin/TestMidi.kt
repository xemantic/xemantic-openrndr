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

package com.xemantic.openrndr.state.midi

import org.openrndr.extra.midi.MidiDeviceDescription

fun main() {
  MidiDevice.matchName("foo")
}
