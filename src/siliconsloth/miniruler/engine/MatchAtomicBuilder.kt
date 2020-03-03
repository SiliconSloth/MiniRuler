package siliconsloth.miniruler.engine

import siliconsloth.miniruler.engine.matching.CompleteMatch

class MatchAtomicBuilder(val match: CompleteMatch): AtomicBuilder() {
    fun maintain(fact: Any) {
        updates.getOrPut(fact::class) { mutableListOf() }
                .add(RuleEngine.Update(fact, true, match))
        match.maintaining.add(fact)
    }
}