package siliconsloth.miniruler.engine

import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import siliconsloth.miniruler.engine.matching.CompleteMatch
import siliconsloth.miniruler.engine.matching.MatchNode
import kotlin.reflect.KClass

class Rule(val bindings: List<Binding<*>>, val fire: (CompleteMatch.() -> Unit)?,
           val end: (CompleteMatch.() -> Unit)?, val engine: RuleEngine) {
    val matches = MatchNode.makeMatchTree(bindings, this)

    val updateHandler = engine.scope.actor<Map<KClass<*>, List<RuleEngine.Update<*>>>>(capacity=UNLIMITED) {
        consumeEach {
            matches.applyUpdates(it)
        }
    }
}