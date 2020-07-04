package siliconsloth.miniruler.timelineviewer

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.KlaxonException
import com.beust.klaxon.Parser
import java.io.File
import java.lang.StringBuilder
import kotlin.math.max

class TimelineLoader(path: String) {
    // Index should always be equal to ID of that match.
    val matches = mutableListOf<Match>()
    val facts = mutableMapOf<String, Fact>()
    val tracks = mutableMapOf<Track.Owner, Track<*,*>>()

    val parser = Parser.default()

    var maxTime = 0

    init {
        File(path).bufferedReader().useLines { lines ->
            for (line in lines) {
                try {
                    val json = parser.parse(StringBuilder(line)) as JsonObject
                    when (json["type"]) {
                        "match" -> parseMatchEvent(json)
                        "fact" -> parseFactEvent(json)
                        else -> error("Unknown event type: ${json["type"]}")
                    }
                    maxTime = max(maxTime, json.int("time")!!)
                } catch (ex: KlaxonException) {
                    println("Warning: Skipping corrupt line in timeline file")
                }
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

        if (bindings != null) {
            val period = track.lastPeriod()
            period.bindings.addAll(bindings)

            for (binding in bindings) {
                when (binding) {
                    is SingletonListing -> binding.period.triggers.add(SingletonListing(period))
                    is MultiListing -> binding.periods.forEach { it.triggers.add(SingletonListing(period)) }
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