package siliconsloth.miniruler.timelineviewer

import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JTextArea

enum class MatchState {
    MATCHED, FIRED, DROPPED, ENDED;
}

const val MATCH_HUE = 0.7f
const val HUE_RANGE = 0.2f
const val PRECISION = 1000f

fun generateHue(obj: Any, base: Float): Float =
        base + (obj.hashCode() * 113 % PRECISION) * (HUE_RANGE / PRECISION)  - HUE_RANGE / 2

data class Match(val rule: String, val bindings: List<String>): Track.Owner {
    override val name = rule
    override val label = "$rule: $bindings"
    override val hue = generateHue(rule, MATCH_HUE)
}

data class Fact(val fact: String, val factClass: String): Track.Owner {
    val triggeredMatches = mutableListOf<Match>()

    override val name = fact
    override val label = fact
    override val hue = generateHue(factClass, MATCH_HUE + 0.5f)
}

data class MatchEvent(override val time: Int, val match: Match, val state: MatchState): Track.Event {
    val inserted = mutableListOf<String>()
    val maintained = mutableListOf<String>()
    val deleted = mutableListOf<String>()
}

data class FactEvent(override val time: Int, val isInsert: Boolean, val maintain: Boolean, val producer: Match?): Track.Event

class FactTrack(owner: Fact): Track<Fact, FactEvent>(owner) {
    override val bindingsTitle = "Triggers"
    override val insertsTitle = "Inserted by"
    override val maintainsTitle = "Maintained by"
    override val deletesTitle = "Deleted by"

    override fun getBindings(period: Period<FactEvent>): List<String>? =
            owner.triggeredMatches.map { it.rule }

    override fun getInserts(period: Period<FactEvent>): List<String> =
            period.events.filter { it.isInsert && !it.maintain }.map { it.producer?.name ?: "N/A" }

    override fun getMaintains(period: Period<FactEvent>): List<String> =
            period.events.filter { it.isInsert && it.maintain }.map { it.producer?.name ?: "N/A" }

    override fun getDeletes(period: Period<FactEvent>): List<String> =
            period.events.filter { !it.isInsert }.map { it.producer?.name ?: "N/A" }
}

class MatchTrack(owner: Match): Track<Match, MatchEvent>(owner) {
    override val bindingsTitle = "Bindings"
    override val insertsTitle = "Inserts"
    override val maintainsTitle = "Maintains"
    override val deletesTitle = "Deletes"

    override fun getBindings(period: Period<MatchEvent>): List<String>? =
            owner.bindings

    override fun getInserts(period: Period<MatchEvent>): List<String> =
            period.events[0].inserted

    override fun getMaintains(period: Period<MatchEvent>): List<String> =
            period.events[0].maintained

    override fun getDeletes(period: Period<MatchEvent>): List<String> =
            period.events[0].deleted
}

fun makeTextArea(text: String = ""): JTextArea {
    val textArea = JTextArea(text)
    textArea.isEditable = false
    textArea.lineWrap = true
    textArea.border = BorderFactory.createEtchedBorder()
    textArea.minimumSize = Dimension(0,0)

    return textArea
}