package siliconsloth.miniruler.engine.matching

import siliconsloth.miniruler.engine.*
import siliconsloth.miniruler.engine.bindings.AggregateBinding
import siliconsloth.miniruler.engine.bindings.Binding
import siliconsloth.miniruler.engine.stores.FactStore
import kotlin.reflect.KClass

class AggregateMatchSet<T: Any>(val binding: AggregateBinding<T>, val nextBindings: List<Binding<*,*>>, rule: Rule): MatchNode(rule) {
    val matches = mutableSetOf<T>().also { matches ->
        @Suppress("UNCHECKED_CAST")
        (rule.engine.stores[binding.type] as FactStore<T>?)?.retrieveMatching(binding.filter)?.forEach {
            matches.add(it)
        }
    }

    var nextMatch: MatchNode = matchRemaining()

    fun matchRemaining(): MatchNode {
        // Copy to ensure immutability despite future changes to matches
        binding.value = HashSet(matches)
        return makeMatchTree(nextBindings, rule)
    }

    override fun applyUpdates(updates: Map<KClass<*>, List<RuleEngine.Update<*>>>) {
        var changed = false

        @Suppress("UNCHECKED_CAST")
        updates[binding.type]?.forEach { (it as RuleEngine.Update<T>).also {
            if (it.isInsert) {
                if (binding.filter.predicate(it.fact)) {
                    matches.add(it.fact)
                    changed = true
                }
            } else {
                matches.remove(it.fact)
                changed = true
            }
        }}

        if (changed) {
            nextMatch.drop()
            nextMatch = matchRemaining()
        } else {
            nextMatch.applyUpdates(updates)
        }
    }

    override fun drop() {
        nextMatch.drop()
    }
}