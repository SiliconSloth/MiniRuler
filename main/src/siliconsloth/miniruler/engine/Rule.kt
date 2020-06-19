package siliconsloth.miniruler.engine

import siliconsloth.miniruler.engine.bindings.Binding
import siliconsloth.miniruler.engine.matching.CompleteMatch
import siliconsloth.miniruler.engine.matching.MatchNode
import kotlin.reflect.KClass

class Rule(val name: String, val bindings: List<Binding<*,*>>, val delay: Int, val debug: Boolean,
           val fire: (CompleteMatch.() -> Unit)?, val end: (CompleteMatch.() -> Unit)?, val engine: RuleEngine) {
    val matches = MatchNode.makeMatchTree(bindings, this)
    var fireCount = 0

    fun applyUpdates(updates: Map<KClass<*>, List<RuleEngine.Update<*>>>) {
        matches.applyUpdates(updates)
    }

    init {
        assert(delay >= 0) { "Negative rule delay" }
    }
}