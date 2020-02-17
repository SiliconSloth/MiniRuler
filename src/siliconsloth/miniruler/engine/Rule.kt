package siliconsloth.miniruler.engine

import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import siliconsloth.miniruler.engine.matches.CompleteMatch
import siliconsloth.miniruler.engine.matches.MatchNode
import kotlin.reflect.KClass

class Rule(val bindings: List<Binding<*>>, val body: CompleteMatch.() -> Unit, val engine: RuleEngine) {
    val matches = MatchNode.makeMatchTree(bindings, this)

    val updateHandler = engine.scope.actor<Map<KClass<*>, List<RuleEngine.Update<*>>>>(capacity=UNLIMITED) {
        consumeEach {
            matches.applyUpdates(it)
        }
    }
}