package siliconsloth.miniruler.engine.matches

import siliconsloth.miniruler.engine.Binding
import siliconsloth.miniruler.engine.Rule
import siliconsloth.miniruler.engine.RuleEngine
import kotlin.reflect.KClass

class InvertedMatchSet<T: Any>(val binding: Binding<T>, val nextBindings: List<Binding<*>>, rule: Rule): MatchNode(rule) {
    val matches = mutableSetOf<T>().also { matches ->
        rule.engine.facts[binding.type]?.filter { binding.condition(it as T) }?.forEach {
            matches.add(it as T)
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
        updates[binding.type]?.forEach { (it as RuleEngine.Update<T>).also {
            if (it.isInsert) {
                if (binding.condition(it.fact)) {
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