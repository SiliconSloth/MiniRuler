package siliconsloth.miniruler.engine.bindings

import siliconsloth.miniruler.engine.Rule
import siliconsloth.miniruler.engine.filters.Filter
import siliconsloth.miniruler.engine.matching.MatchSet
import kotlin.reflect.KClass

class SimpleBinding<T: Any>(type: KClass<T>, filter: Filter<T>): Binding<T,T>(type, filter) {
    override fun makeMatchNode(nextBindings: List<Binding<*,*>>, rule: Rule) =
            MatchSet(this, nextBindings, rule)
}