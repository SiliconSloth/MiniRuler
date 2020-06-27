package siliconsloth.miniruler.timelineviewer

import com.beust.klaxon.JsonArray
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
        val bindings = json.array<Any?>("bindings")?.map { deserializeBindValue(it) }

        val match: Match
        if (state == MatchState.MATCHED) {
            if (id != matches.size) {
                error("Bad match ID $id, expected ${matches.size}")
            }

            match = Match(json.string("rule")!!, bindings!!.map { it.toString() })
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
        val period = track.lastPeriod()

        if (bindings != null) {
            period.bindings.addAll(bindings)

            for (binding in bindings) {
                when (binding) {
                    is SingletonListing -> binding.period.bindings.add(SingletonListing(period))
                    is MultiListing -> binding.periods.forEach { it.bindings.add(SingletonListing(period)) }
                }
            }
        }
    }

    fun deserializeBindValue(value: Any?): InfoListing =
            @Suppress("UNCHECKED_CAST")
            when (value) {
                is String -> SingletonListing(tracks[facts[value]!!]!!.lastPeriod())
                null -> EmptyListing()
                is JsonArray<*> -> MultiListing(value.map { tracks[facts[it]!!]!!.lastPeriod() })
                else -> error("Bad bind value: $value")
            }

    fun parseFactEvent(json: JsonObject) {
        val fact = json.string("fact")!!
        val fClass = json.string("class")!!
        val producer = json.int("producer")?.let { tracks[matches[it]]!!.lastPeriod() }
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
        val period = track.lastPeriod()

        val updateList = when {
            isInsert && !isMaintained -> Track.Period<*>::inserts
            isInsert && isMaintained -> Track.Period<*>::maintains
            !isInsert && !isMaintained -> Track.Period<*>::deletes
            else -> error("Maintained deletes are not allowed")
        }

        if (producer != null) {
            updateList.get(period).add(SingletonListing(producer))
            updateList.get(producer).add(SingletonListing(period))
        } else {
            updateList.get(period).add(EmptyListing())
        }
    }
}

fun main() {
    val viewer = TimelineViewer("timeline.jtl")
    viewer.isVisible = true
}