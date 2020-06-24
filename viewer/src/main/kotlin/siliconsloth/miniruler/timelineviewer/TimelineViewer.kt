package siliconsloth.miniruler.timelineviewer

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.awt.*
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.math.max

class TimelineViewer(inputPath: String): JFrame("MiniRuler Timeline Recorder") {
    val matches = mutableListOf<Match>()
    val tracks = mutableMapOf<Track.Owner, Track<*,*>>()

    val parser = Parser.default()

    var maxTime = 0

    lateinit var searchField: JTextField
    lateinit var timelinePane: TimelinePane

    init {
        loadTimeline(inputPath)

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout()

        makeSearchBar()
        makeViewport()

        pack()
    }

    fun makeSearchBar() {
        searchField = JTextField()
        searchField.document.addDocumentListener(SearchFieldListener())
        add(searchField, BorderLayout.PAGE_START)
    }

    fun makeViewport() {
        timelinePane = TimelinePane(tracks.values.toList(), maxTime)
        val scrollPane = InteractiveScrollPane(timelinePane)
        scrollPane.viewport.scrollMode = JViewport.SIMPLE_SCROLL_MODE

        scrollPane.addMouseListener(timelinePane)
        scrollPane.addMouseMotionListener(timelinePane)

        add(scrollPane)
    }

    inner class SearchFieldListener: DocumentListener {
        override fun insertUpdate(e: DocumentEvent) {
            timelinePane.updateFilter(searchField.text)
        }

        override fun removeUpdate(e: DocumentEvent) {
            timelinePane.updateFilter(searchField.text)
        }

        override fun changedUpdate(e: DocumentEvent) {
        }
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

        tracks.values.forEach { it.finalize(maxTime) }
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

        val event = MatchEvent(json.int("time")!!, match, state)
        val track = tracks.getOrPut(match) { Track<Match, MatchEvent>(match) }

        @Suppress("UNCHECKED_CAST")
        (track as Track<Match, MatchEvent>).addEvent(event)
        if (state == MatchState.ENDED) {
            track.closePeriod()
        }
    }

    fun parseFactEvent(json: JsonObject) {
        val fact = json.string("fact")!!
        val fClass = json.string("class")!!
        val producer = json.int("producer")?.let { matches[it] }

        val owner = Fact(fact, fClass)
        val event = FactEvent(json.int("time")!!, json.boolean("insert")!!, json.boolean("maintain")!!, producer)
        val track = tracks.getOrPut(owner) { Track<Fact, FactEvent>(owner) }

        @Suppress("UNCHECKED_CAST")
        (track as Track<Fact, FactEvent>).addEvent(event)
        if (!event.isInsert) {
            track.closePeriod()
        }
    }
}

fun main() {
    val viewer = TimelineViewer("timeline.jtl")
    viewer.isVisible = true
}