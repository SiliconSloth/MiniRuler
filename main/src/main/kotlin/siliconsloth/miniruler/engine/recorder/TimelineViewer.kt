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
    data class Match(val rule: String, val bindings: List<String>)
    data class MatchEvent(val match: Match, val state: CompleteMatch.State, val time: Int)
    data class FactEvent(val fact: String, val isInsert: Boolean, val maintain: Boolean,
                               val producer: Match?, val time: Int)

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
        val id = json.int("id")!!
        val state = CompleteMatch.State.valueOf(json.string("state")!!)

        val match: Match
        if (state == CompleteMatch.State.MATCHED) {
            if (id != matches.size) {
                error("Bad match ID $id, expected ${matches.size}")
            }

            match = Match(json.string("rule")!!, ArrayList(json.array("bindings")!!))
            matches.add(match)
        } else {
            match = matches[id]
        }

        val event = MatchEvent(match, state, json.int("time")!!)
        matchEvents.add(event)
    }

    fun parseFactEvent(json: JsonObject) {
        val producer = json.int("producer")?.let { matches[it] }
        val event = FactEvent(json.string("fact")!!, json.boolean("insert")!!, json.boolean("maintain")!!,
                producer, json.int("time")!!)
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