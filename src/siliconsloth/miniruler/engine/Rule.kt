package siliconsloth.miniruler.engine

import siliconsloth.miniruler.engine.bindings.Binding
import siliconsloth.miniruler.engine.matching.CompleteMatch
import siliconsloth.miniruler.engine.matching.MatchNode
import kotlin.reflect.KClass

class Rule(val bindings: List<Binding<*,*>>, val fire: (CompleteMatch.() -> Unit)?,
           val end: (CompleteMatch.() -> Unit)?, val engine: RuleEngine) {
    val matches = MatchNode.makeMatchTree(bindings, this)

    fun applyUpdates(updates: Map<KClass<*>, List<RuleEngine.Update<*>>>) {
        matches.applyUpdates(updates)
    }
}