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
    override val name = fact
    override val label = fact
    override val hue = generateHue(factClass, MATCH_HUE + 0.5f)
}

data class MatchEvent(override val time: Int, val match: Match, val state: MatchState): Track.Event

data class FactEvent(override val time: Int, val isInsert: Boolean, val maintain: Boolean, val producer: Track.Period<*>?): Track.Event

class FactTrack(owner: Fact): Track<Fact, FactEvent>(owner) {
    override val bindingsTitle = "Triggered"
    override val insertsTitle = "Inserted by"
    override val maintainsTitle = "Maintained by"
    override val deletesTitle = "Deleted by"
}

class MatchTrack(owner: Match): Track<Match, MatchEvent>(owner) {
    override val bindingsTitle = "Bindings"
    override val insertsTitle = "Inserted"
    override val maintainsTitle = "Maintained"
    override val deletesTitle = "Deleted"
}

fun makeTextArea(text: String = ""): JTextArea {
    val textArea = JTextArea(text)
    textArea.isEditable = false
    textArea.lineWrap = true
    textArea.border = BorderFactory.createEtchedBorder()
    textArea.minimumSize = Dimension(0,0)

    return textArea
}