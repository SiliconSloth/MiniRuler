package siliconsloth.miniruler.engine.matching

import siliconsloth.miniruler.engine.Binding
import siliconsloth.miniruler.engine.Rule
import siliconsloth.miniruler.engine.RuleEngine
import kotlin.reflect.KClass

abstract class MatchNode(val rule: Rule) {
    abstract fun applyUpdates(updates: Map<KClass<*>, List<RuleEngine.Update<*>>>)

    abstract fun drop()

    companion object {
        fun makeMatchTree(bindings: List<Binding<*>>, rule: Rule) = when {
            bindings.isEmpty() -> CompleteMatch(rule)
            bindings[0].inverted -> InvertedMatchSet(bindings[0], bindings.drop(1), rule)
            else -> MatchSet(bindings[0], bindings.drop(1), rule)
        }
    }
}