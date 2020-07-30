package siliconsloth.miniruler.engine.matching

import siliconsloth.miniruler.engine.*
import siliconsloth.miniruler.engine.bindings.Binding
import siliconsloth.miniruler.engine.bindings.InvertedBinding
import siliconsloth.miniruler.engine.stores.FactStore
import kotlin.reflect.KClass

class InvertedMatchSet<T: Any>(val binding: InvertedBinding<T>, val nextBindings: List<Binding<*,*>>, rule: Rule): MatchNode(rule) {
    val matches = mutableSetOf<T>().also { matches ->
        @Suppress("UNCHECKED_CAST")
        (rule.engine.stores[binding.type] as FactStore<T>?)?.retrieveMatching(binding.filter)?.forEach {
            matches.add(it)
        }
    }

    var nextMatch: MatchNode? = null

    init {
        if (matches.isEmpty()) {
            matchRemaining()
        }
    }

    fun matchRemaining() {
        nextMatch = makeMatchTree(nextBindings, rule)
    }

    override fun applyUpdates(updates: Map<KClass<*>, List<RuleEngine.Update<*>>>) {
        @Suppress("UNCHECKED_CAST")
        updates[binding.type]?.forEach { (it as RuleEngine.Update<T>).also {
            if (it.isInsert) {
                if (binding.filter.predicate(it.fact)) {
                    matches.add(it.fact)
                }
            } else {
                matches.remove(it.fact)
            }
        }}

        if (matches.isEmpty()) {
            nextMatch?.applyUpdates(updates) ?: matchRemaining()
        } else {
            nextMatch?.drop()
            nextMatch = null
        }
    }

    override fun drop() {
        nextMatch?.drop()
    }
}