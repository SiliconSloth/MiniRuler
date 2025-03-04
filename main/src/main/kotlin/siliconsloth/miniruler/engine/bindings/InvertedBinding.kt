package siliconsloth.miniruler.engine.bindings

import siliconsloth.miniruler.engine.Rule
import siliconsloth.miniruler.engine.filters.Filter
import siliconsloth.miniruler.engine.matching.InvertedMatchSet
import kotlin.reflect.KClass

class InvertedBinding<T: Any>(type: KClass<T>, filter: Filter<T>): Binding<T, Nothing>(type, filter) {
    override fun makeMatchNode(nextBindings: List<Binding<*,*>>, rule: Rule) =
            InvertedMatchSet(this, nextBindings, rule)
}