package siliconsloth.miniruler.engine.recorder

import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.matching.CompleteMatch

class Recorder(val outputPath: String) {
    fun recordMatchState(match: CompleteMatch) {
        println("ID: ${match.id} State: ${match.state}")
        if (match.state == CompleteMatch.State.MATCHED) {
            println(match.rule.name)
            println(match.bindValues.values)
        }
    }

    fun recordUpdate(update: RuleEngine.Update<*>) {
        println("Fact: ${update.fact} Producer: ${update.producer?.id} Insert: ${update.isInsert} Maintain: ${update.maintain}")
    }
}