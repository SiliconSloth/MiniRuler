package siliconsloth.miniruler.engine

import siliconsloth.miniruler.engine.filters.Filter
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

data class Binding<T: Any>(val type: KClass<T>, val filter: Filter<T>, val inverted: Boolean = false) {
    var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value!!
    }
}