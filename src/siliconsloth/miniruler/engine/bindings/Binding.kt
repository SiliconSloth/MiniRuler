package siliconsloth.miniruler.engine.bindings

import siliconsloth.miniruler.engine.Rule
import siliconsloth.miniruler.engine.filters.Filter
import siliconsloth.miniruler.engine.matching.MatchNode
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class Binding<T: Any, V>(val type: KClass<T>, val filter: Filter<T>) {
    var value: V? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return value!!
    }

    abstract fun makeMatchNode(nextBindings: List<Binding<*,*>>, rule: Rule): MatchNode
}