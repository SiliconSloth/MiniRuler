package siliconsloth.miniruler.engine.recorder

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import siliconsloth.miniruler.engine.matching.CompleteMatch
import java.awt.Dimension
import java.io.File
import java.lang.StringBuilder
import javax.swing.JFrame
import javax.swing.JPanel

class TimelineViewer(inputPath: String): JPanel() {
    data class Match(val id: Int, val rule: String, val bindings: List<String>)
    data class MatchEvent(val id: Int, val state: CompleteMatch.State, val time: Int)
    data class FactEvent(val fact: String, val isInsert: Boolean, val maintain: Boolean,
                               val producer: Int?, val time: Int)

    val matches = mutableListOf<Match>()
    val matchEvents = mutableSetOf<MatchEvent>()
    val factEvents = mutableSetOf<FactEvent>()

    val parser = Parser.default()

    init {
        preferredSize = Dimension(1800, 1000)
        loadTimeline(inputPath)

        println(matches)
        println(matchEvents)
        println(factEvents)
    }

    fun loadTimeline(path: String) {
        File(path).bufferedReader().useLines { lines ->
            for (line in lines) {
                val json = parser.parse(StringBuilder(line)) as JsonObject
                when (json["type"]) {
                    "match" -> parseMatchEvent(json)
                    "fact" -> parseFactEvent(json)
                    else -> error("Unknown event type: ${json["type"]}")
                }
            }
        }
    }

    fun parseMatchEvent(json: JsonObject) {
        val event = MatchEvent(json.int("id")!!, CompleteMatch.State.valueOf(json.string("state")!!),
                json.int("time")!!)
        matchEvents.add(event)

        if (event.state == CompleteMatch.State.MATCHED) {
            if (event.id != matches.size) {
                error("Bad match ID ${event.id}, expected ${matches.size}")
            }

            matches.add(Match(event.id, json.string("rule")!!, ArrayList(json.array("bindings")!!)))
        }
    }

    fun parseFactEvent(json: JsonObject) {
        val event = FactEvent(json.string("fact")!!, json.boolean("insert")!!, json.boolean("maintain")!!,
                json.int("producer"), json.int("time")!!)
        factEvents.add(event)
    }
}

fun main() {
    val viewer = TimelineViewer("timeline.json")

    val frame = JFrame("MiniRuler Timeline Viewer")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.add(viewer)
    frame.pack()
    frame.isVisible = true
}