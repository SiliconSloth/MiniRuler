package siliconsloth.miniruler.engine.builders

import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.matching.CompleteMatch

class MatchAtomicBuilder(engine: RuleEngine, match: CompleteMatch): AtomicBuilder(engine, match) {
    fun maintain(fact: Any) {
        updates.getOrPut(fact::class) { mutableListOf() }
                .add(RuleEngine.Update(fact, true, true, match))
        match!!.maintaining.add(fact)
    }
}