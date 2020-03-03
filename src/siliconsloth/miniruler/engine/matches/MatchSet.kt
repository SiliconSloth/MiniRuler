package siliconsloth.miniruler.engine.matches

import siliconsloth.miniruler.engine.*
import kotlin.reflect.KClass

class MatchSet<T: Any>(val binding: Binding<T>, val nextBindings: List<Binding<*>>, rule: Rule): MatchNode(rule) {
    val matches = mutableMapOf<T, MatchNode>()

    init {
        (rule.engine.stores[binding.type] as FactStore<T>?)?.retrieveMatching(binding.filter)?.forEach {
            matchRemaining(it as T)
        }
    }

    fun matchRemaining(bindValue: T) {
        binding.value = bindValue
        matches[bindValue] = makeMatchTree(nextBindings, rule)
    }

    override fun applyUpdates(updates: Map<KClass<*>, List<RuleEngine.Update<*>>>) {
        val added = mutableListOf<T>()
        updates[binding.type]?.forEach { (it as RuleEngine.Update<T>).also {
            if (it.isInsert) {
                if (binding.filter.predicate(it.fact)) {
                    added.add(it.fact)
                    matchRemaining(it.fact)
                }
            } else {
                binding.value = it.fact
                matches.remove(it.fact)?.drop()
            }
        }}

        matches.filterKeys { it !in added }.forEach {
            binding.value = it.key
            it.value.applyUpdates(updates)
        }
    }

    override fun drop() = matches.forEach {
        binding.value = it.key
        it.value.drop()
    }
}