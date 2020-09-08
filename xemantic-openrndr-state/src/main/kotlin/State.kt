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

package com.xemantic.openrndr.state

import mu.KotlinLogging
import org.openrndr.Extension
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType

annotation class Range(val min: Double, val max: Double)

annotation class Transition(val animatable: Boolean = true)

abstract class Property<T>(
    val name: String,
    val type: Type,
    val transition: Transition
) {
  enum class Type {
    DOUBLE,
    BOOLEAN,
    STRING
  }
}

class DoubleProperty(
    name: String,
    transition: Transition,
    val min: Double,
    val max: Double
) : Property<Double>(name, Type.DOUBLE, transition)

class BooleanProperty(
    name: String,
    transition: Transition
) : Property<Boolean>(name, Type.BOOLEAN, transition)

class StringProperty(
    name: String,
    transition: Transition
) : Property<String>(name, Type.STRING, transition)

interface StateConsumer {
  fun updateState(property: String, value: Double)
  fun updateState(property: String, value: Boolean)
  fun updateState(property: String, value: String)
}

interface StateProducer<T : Any> {
  fun initialize(consumer: StateConsumer)
  fun updateState(state: T)
}

class StateCurator<T : Any>(
    val state: T,
    val metadata: StateMetadata<T>,
    private val stateProducers: Collection<StateProducer<T>>
) : Extension {
  override var enabled: Boolean = true
  private val logger = KotlinLogging.logger {}
  private val consumers = arrayListOf<StateConsumer>()
  init {
    val stateUpdater = object : StateConsumer {
      override fun updateState(property: String, value: Double) { update(property, value) }
      override fun updateState(property: String, value: Boolean) { update(property, value) }
      override fun updateState(property: String, value: String) { update(property, value) }
      private fun update(name: String, value: Any) {
        val property = metadata.accessorMap[name]!!
        val current = property.get(state)
        if (current != value) {
          logger.debug { "Setting property: $name [$current->$value]" }
          property.set(state, value)
          when (metadata.propertyMap[name]!!.type) {
            Property.Type.DOUBLE -> consumers.forEach { it.updateState(name, value as Double) }
            Property.Type.BOOLEAN -> consumers.forEach { it.updateState(name, value as Boolean) }
            Property.Type.STRING -> consumers.forEach { it.updateState(name, value as String) }
          }
        }
      }
    }
    stateProducers.forEach { producer ->
        producer.initialize(stateUpdater)
        producer.updateState(state)
    }
  }
  fun getState(property: String): Any {
    return metadata.accessorMap[property]!!.get(state)
  }
  fun addStateConsumer(consumer: StateConsumer) {
    consumers.add(consumer)
  }
  fun updateStateProducers() {
    stateProducers.forEach { producer ->
      producer.updateState(state)
    }
  }
}

class StateMetadata<T : Any>(private val state: T) {
  @Suppress("IMPLICIT_CAST_TO_ANY")
  val propertyMap by lazy {
    state::class.declaredMemberProperties
        .filterIsInstance<KMutableProperty1<T, Any>>()
        .map { field ->
          val transition = field.findAnnotation<Transition>() ?: Transition::class.createInstance()
          val property = when (field.returnType.javaType) {
            Double::class.java -> {
              val range = field.findAnnotation<Range>()
                  ?: throw IllegalStateException("No Range annotation for property: ${field.name}")
              DoubleProperty(field.name, transition, range.min, range.max)
            }
            Boolean::class.java -> { BooleanProperty(field.name, transition) }
            String::class.java -> { StringProperty(field.name, transition) }
            else -> throw IllegalStateException(
                "Unsupported property type, property: ${field.name}, type: ${field.returnType.javaType}"
            )
          }
          Pair(property.name, property)
        }
        .toMap()
  }
  val accessorMap by lazy {
    state::class.declaredMemberProperties
        .filterIsInstance<KMutableProperty1<T, Any>>()
        .map { property -> Pair(property.name, property) }
        .toMap()
  }
}
