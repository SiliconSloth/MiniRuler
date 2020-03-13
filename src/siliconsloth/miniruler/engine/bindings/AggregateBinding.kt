package siliconsloth.miniruler.engine.bindings

import siliconsloth.miniruler.engine.Rule
import siliconsloth.miniruler.engine.filters.Filter
import siliconsloth.miniruler.engine.matching.AggregateMatchSet
import siliconsloth.miniruler.engine.matching.MatchNode
import kotlin.reflect.KClass

class AggregateBinding<T: Any>(type: KClass<T>, filter: Filter<T>): Binding<T, Iterable<T>>(type, filter) {
    override fun makeMatchNode(nextBindings: List<Binding<*, *>>, rule: Rule): MatchNode =
            AggregateMatchSet(this, nextBindings, rule)
}