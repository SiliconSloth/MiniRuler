package siliconsloth.miniruler.engine.matching

import siliconsloth.miniruler.engine.*
import siliconsloth.miniruler.engine.stores.FactStore
import kotlin.reflect.KClass

class MatchSet<T: Any>(val binding: Binding<T>, val nextBindings: List<Binding<*>>, rule: Rule): MatchNode(rule) {
    class FreshNode(val node: MatchNode) {
        var fresh = true
    }

    val matches = mutableMapOf<T, FreshNode>()

    init {
        (rule.engine.stores[binding.type] as FactStore<T>?)?.retrieveMatching(binding.filter)?.forEach {
            matchRemaining(it as T)
        }
    }

    fun matchRemaining(bindValue: T) {
        binding.value = bindValue
        matches[bindValue] = FreshNode(makeMatchTree(nextBindings, rule))
    }

    override fun applyUpdates(updates: Map<KClass<*>, List<RuleEngine.Update<*>>>) {
        updates[binding.type]?.forEach { (it as RuleEngine.Update<T>).also {
            if (it.isInsert) {
                if (binding.filter.predicate(it.fact) && !matches.containsKey(it.fact)) {
                    matchRemaining(it.fact)
                }
            } else {
                binding.value = it.fact
                matches.remove(it.fact)?.node?.drop()
            }
        }}

        if (nextBindings.isNotEmpty()) {
            matches.forEach {
                if (it.value.fresh) {
                    it.value.fresh = false
                } else {
                    binding.value = it.key
                    it.value.node.applyUpdates(updates)
                }
            }
        }
    }

    override fun drop() = matches.forEach {
        binding.value = it.key
        it.value.node.drop()
    }
}