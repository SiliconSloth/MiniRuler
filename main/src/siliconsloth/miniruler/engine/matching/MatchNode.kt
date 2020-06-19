package siliconsloth.miniruler.engine.matching

import siliconsloth.miniruler.engine.bindings.Binding
import siliconsloth.miniruler.engine.Rule
import siliconsloth.miniruler.engine.RuleEngine
import kotlin.reflect.KClass

abstract class MatchNode(val rule: Rule) {
    abstract fun applyUpdates(updates: Map<KClass<*>, List<RuleEngine.Update<*>>>)

    abstract fun drop()

    companion object {
        fun makeMatchTree(bindings: List<Binding<*,*>>, rule: Rule) =
                if (bindings.isEmpty()) {
                    CompleteMatch(rule)
                } else {
                    bindings[0].makeMatchNode(bindings.drop(1), rule)
                }
    }
}