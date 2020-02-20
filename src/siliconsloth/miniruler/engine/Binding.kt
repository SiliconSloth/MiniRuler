package siliconsloth.miniruler.engine

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

data class Binding<T: Any>(val type: KClass<T>, val condition: (T) -> Boolean, val inverted: Boolean = false) {
    var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value!!
    }
}