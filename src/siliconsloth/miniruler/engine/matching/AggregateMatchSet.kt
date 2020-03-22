package siliconsloth.miniruler.engine.matching

import siliconsloth.miniruler.engine.*
import siliconsloth.miniruler.engine.bindings.AggregateBinding
import siliconsloth.miniruler.engine.bindings.Binding
import siliconsloth.miniruler.engine.stores.FactStore
import kotlin.reflect.KClass

class AggregateMatchSet<T: Any>(val binding: AggregateBinding<T>, val nextBindings: List<Binding<*,*>>, rule: Rule): MatchNode(rule) {
    val matches = mutableSetOf<T>().also { matches ->
        (rule.engine.stores[binding.type] as FactStore<T>?)?.retrieveMatching(binding.filter)?.forEach {
            matches.add(it)
        }
    }

    var nextMatch: MatchNode = matchRemaining()

    fun matchRemaining(): MatchNode {
        binding.value = matches
        return makeMatchTree(nextBindings, rule)
    }

    override fun applyUpdates(updates: Map<KClass<*>, List<RuleEngine.Update<*>>>) {
        nextMatch.drop()

        updates[binding.type]?.forEach { (it as RuleEngine.Update<T>).also {
            if (it.isInsert) {
                if (binding.filter.predicate(it.fact)) {
                    matches.add(it.fact)
                }
            } else {
                matches.remove(it.fact)
            }
        }}

        nextMatch = matchRemaining()
    }

    override fun drop() {
        nextMatch.drop()
    }
}