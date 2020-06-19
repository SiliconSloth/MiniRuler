package siliconsloth.miniruler.engine.recorder

import com.beust.klaxon.JsonObject
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.matching.CompleteMatch
import java.io.File

class TimelineRecorder(outputPath: String) {
    val writer = File(outputPath).bufferedWriter()

    var timestep = 0

    fun tick() =
            timestep++

    fun recordMatchState(match: CompleteMatch) {
        val fields = mutableMapOf<String, Any>(
                "type" to "match",
                "id" to match.id,
                "state" to match.state,
                "time" to timestep
        )
        if (match.state == CompleteMatch.State.MATCHED) {
            fields["rule"] = match.rule.name
            fields["bindings"] = match.bindValues.map { it.toString() }
        }

        writeObject(fields)
    }

    fun recordUpdate(update: RuleEngine.Update<*>) {
        val fields = mutableMapOf<String, Any>(
                "type" to "fact",
                "fact" to update.fact.toString(),
                "insert" to update.isInsert,
                "maintain" to update.maintain
        )
        update.producer?.let { fields["producer"] = it.id }

        writeObject(fields)
    }

    fun writeObject(fields: Map<String, Any?>) {
        writer.write(JsonObject(fields).toJsonString()+"\n")
    }
}