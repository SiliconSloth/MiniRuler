package siliconsloth.miniruler.engine

import com.beust.klaxon.JsonObject
import siliconsloth.miniruler.engine.bindings.AggregateBinding
import siliconsloth.miniruler.engine.bindings.Binding
import siliconsloth.miniruler.engine.bindings.InvertedBinding
import siliconsloth.miniruler.engine.bindings.SimpleBinding
import siliconsloth.miniruler.engine.matching.CompleteMatch
import java.io.File

class TimelineRecorder(outputPath: String) {
    val writer = File(outputPath).bufferedWriter()

    var timestep = 0

    init {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                writer.close()
            }
        })
    }

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
            fields["bindings"] = match.rule.bindings.zip(match.bindValues) { b,v -> serializeBinding(b,v) }
        }

        writeObject(fields)
    }

    fun recordUpdate(update: RuleEngine.Update<*>) {
        val fields = mutableMapOf<String, Any>(
                "type" to "fact",
                "class" to (update.fact::class.simpleName ?: "null"),
                "fact" to update.fact.toString(),
                "insert" to update.isInsert,
                "maintain" to update.maintain,
                "time" to timestep
        )
        update.producer?.let { fields["producer"] = it.id }

        writeObject(fields)
    }

    fun serializeBinding(binding: Binding<*,*>, value: Any?): Any? =
            when (binding) {
                is SimpleBinding<*> -> value.toString()
                is InvertedBinding<*> -> null
                is AggregateBinding<*> -> (value as Iterable<*>).map { it.toString() }
                else -> error("Unknown binding class: ${binding::class}")
            }

    fun writeObject(fields: Map<String, Any?>) {
        writer.write(JsonObject(fields).toJsonString()+"\n")
    }
}