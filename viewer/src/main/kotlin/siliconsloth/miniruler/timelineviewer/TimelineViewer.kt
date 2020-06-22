package siliconsloth.miniruler.timelineviewer

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.awt.*
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import javax.swing.*
import kotlin.math.max

class TimelineViewer(inputPath: String): JPanel() {
    val matches = mutableListOf<Match>()
    val matchEvents = mutableSetOf<MatchEvent>()
    val factTracks = mutableMapOf<Pair<String, String>, Track>()

    val parser = Parser.default()

    var maxTime = 0

    init {
        loadTimeline(inputPath)

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        layout = BorderLayout()

        val timelinePane = TimelinePane(factTracks.values.toList(), maxTime)
        val scrollPane = InteractiveScrollPane(timelinePane)
        scrollPane.viewport.scrollMode = JViewport.SIMPLE_SCROLL_MODE

        scrollPane.addMouseListener(timelinePane)
        scrollPane.addMouseMotionListener(timelinePane)

        add(scrollPane)
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
                maxTime = max(maxTime, json.int("time")!!)
            }
        }

        factTracks.values.forEach { it.finalize(maxTime) }
    }

    fun parseMatchEvent(json: JsonObject) {
        val id = json.int("id")!!
        val state = MatchState.valueOf(json.string("state")!!)

        val match: Match
        if (state == MatchState.MATCHED) {
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
        val fact = json.string("fact")!!
        val fClass = json.string("class")!!
        val producer = json.int("producer")?.let { matches[it] }

        val event = FactEvent(json.boolean("insert")!!, json.boolean("maintain")!!, producer, json.int("time")!!)
        val track = factTracks.getOrPut(fact to fClass) { Track(fact, fClass) }

        track.addEvent(event)
        if (!event.isInsert) {
            track.closePeriod()
        }
    }
}

fun main() {
    val viewer = TimelineViewer("timeline.jtl")

    val frame = JFrame("MiniRuler Timeline Recorder")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.add(viewer)
    frame.pack()
    frame.isVisible = true
}