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

class TimelineViewer(inputPath: String): JFrame("MiniRuler Timeline Recorder"), TimelinePane.SelectionListener {
    val matches = mutableListOf<Match>()
    val facts = mutableMapOf<String, Fact>()
    val tracks = mutableMapOf<Track.Owner, Track<*,*>>()

    val parser = Parser.default()

    var maxTime = 0

    val searchField: JTextField
    val timelinePane: TimelinePane
    val infoPanel: InfoPanel

    init {
        loadTimeline(inputPath)

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout()

        val leftPanel = JPanel(BorderLayout())

        searchField = JTextField()
        searchField.document.addDocumentListener(SearchFieldListener())
        leftPanel.add(searchField, BorderLayout.PAGE_START)

        timelinePane = TimelinePane(tracks.values.toList(), maxTime)
        timelinePane.addSelectionListener(this)

        val scrollPane = InteractiveScrollPane(timelinePane)
        scrollPane.viewport.scrollMode = JViewport.SIMPLE_SCROLL_MODE
        scrollPane.addMouseListener(timelinePane)
        scrollPane.addMouseMotionListener(timelinePane)
        timelinePane.scrollPane = scrollPane

        leftPanel.add(scrollPane)

        infoPanel = InfoPanel()
        val infoScrollPane = JScrollPane(infoPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        infoPanel.scrollPane = infoScrollPane

        add(JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, infoScrollPane))
        pack()
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

    override fun periodSelected(period: Track.Period<*>?) {
        infoPanel.period = period
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
        val track = tracks.getOrPut(match) { MatchTrack(match) }

        @Suppress("UNCHECKED_CAST")
        (track as Track<Match, MatchEvent>).addEvent(event)
        if (state == MatchState.ENDED) {
            track.closePeriod()
        }

        for (fact in match.bindings) {
            facts[fact]?.triggeredMatches?.add(match)
        }
    }

    fun parseFactEvent(json: JsonObject) {
        val fact = json.string("fact")!!
        val fClass = json.string("class")!!
        val producer = json.int("producer")?.let { matches[it] }
        val isInsert = json.boolean("insert")!!
        val isMaintained = json.boolean("maintain")!!

        val owner = Fact(fact, fClass)
        val event = FactEvent(json.int("time")!!, isInsert, isMaintained, producer)
        val track = tracks.getOrPut(owner) { FactTrack(owner) }
        facts[fact] = owner

        @Suppress("UNCHECKED_CAST")
        (track as Track<Fact, FactEvent>).addEvent(event)
        if (!event.isInsert) {
            track.closePeriod()
        }

        if (producer != null) {
            val prodEvent = tracks[producer]!!.periods.last { it.events.isNotEmpty() }.events[0] as MatchEvent
            when {
                isInsert && !isMaintained -> prodEvent.inserted.add(fact)
                isInsert && isMaintained -> prodEvent.maintained.add(fact)
                !isInsert && !isMaintained -> prodEvent.deleted.add(fact)
                else -> error("Maintained deletes are not allowed")
            }
        }
    }
}

fun main() {
    val viewer = TimelineViewer("timeline.jtl")
    viewer.isVisible = true
}