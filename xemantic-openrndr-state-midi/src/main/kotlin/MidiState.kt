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

import com.xemantic.openrndr.state.*
import mu.KotlinLogging
import org.openrndr.extra.midi.MidiDeviceDescription
import org.openrndr.extra.midi.MidiTransceiver
import org.openrndr.math.map

class MidiDevice(
    val name: String,
    val vendor: String
) {
  companion object {
    private val logger = KotlinLogging.logger {}
    fun matchName(regex: String): MidiDevice? {
      val regEx = Regex(regex)
      return MidiDeviceDescription.list()
          .find { description -> description.name.matches(regEx) }
          ?.let { description -> MidiDevice(description.name, description.vendor)
          } ?: run {
            logger.warn { "MIDI device not matched: $regex" }
            logger.info { "Listing available MIDI devices:" }
            MidiDeviceDescription.list().forEach {
              logger.info { " |-name: '${it.name}', vendor: '${it.vendor}', receiver:${it.receive}, transmitter:${it.transmit}" }
            }
            null
          }
    }
  }
}

class MidiStateProducer<T : Any>(
    val device: MidiDevice,
    private val metadata: StateMetadata<T>,
    private val midiMapping: MidiMapping) : StateProducer<T> {
  private val logger = KotlinLogging.logger {}
  private val controller = MidiTransceiver.fromDeviceVendor(device.name, device.vendor)
  override fun initialize(consumer: StateConsumer) {
      controller.controlChanged.listen { event ->
        val name = midiMapping.controlMap[event.control]
        if (name == null) {
          logger.debug { "event - type: ${event.eventType}, note: ${event.note}, channel ${event.channel}, control: ${event.control}, origin: ${event.origin}, value: ${event.value}, velocity: ${event.velocity}" }
        } else {
          val property = metadata.propertyMap[name]!!
          when (property.type) {
            Property.Type.DOUBLE -> {
              val doubleProperty = property as DoubleProperty
              val value = map(
                  0.0,
                  127.0,
                  doubleProperty.min,
                  doubleProperty.max,
                  event.value.toDouble()
              )
              consumer.updateState(name, value)
            }
            Property.Type.BOOLEAN -> consumer.updateState(name, event.value == 127)
            Property.Type.STRING -> throw UnsupportedOperationException("unsupported in midi, should never happen")
          }
        }
      }
    // TODO validation, warning on unused properties
    //midiMapping.controlMap.forEach { control, property -> propertyMap[property]}
  }
  override fun updateState(state: T) {
    logger.debug { "Updating MIDI state" }
    midiMapping.controlMap.forEach { control, name ->
      val property = metadata.propertyMap[name]
          ?: throw IllegalStateException("MIDI mapped property not defined in the state class: $name")
      val value = metadata.accessorMap[name]!!.get(state)
      val initialValue = when (property.type) {
        Property.Type.DOUBLE -> {
          val doubleProperty = property as DoubleProperty
          map(
              doubleProperty.min,
              doubleProperty.max,
              0.0,
              127.0,
              value as Double
          ).toInt()
        }
        Property.Type.BOOLEAN -> {
          if (value as Boolean) 127 else 0
        }
        Property.Type.STRING -> throw UnsupportedOperationException("unsupported in midi, should never happen")
      }
      controller.controlChange(0, control, initialValue)
    }
  }
}

fun midiMapping(init: MidiMappingBuilder.() -> Unit): MidiMapping {
  val builder = MidiMappingBuilder()
  builder.init()
  return MidiMapping(builder.mapBuilder.toMap())
}

class MidiMapping(
    val controlMap: Map<Int, String>
)

class MidiMappingBuilder {
  val mapBuilder = mutableMapOf<Int, String>()
  fun control(no: Int, property: String) {
    mapBuilder.put(no, property)
  }
}
