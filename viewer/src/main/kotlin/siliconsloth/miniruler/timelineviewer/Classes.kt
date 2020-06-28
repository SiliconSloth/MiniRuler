package siliconsloth.miniruler.timelineviewer

import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JTextArea

enum class MatchState {
    MATCHED, FIRED, DROPPED, ENDED;
}

/**
 * Base hue for match tracks. Facts use opposite base hue.
 */
const val MATCH_HUE = 0.7f
/**
 * Range of possible hues around base.
 */
const val HUE_RANGE = 0.2f
/**
 * Number of possible hues for each of the two track types.
 */
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

data class MatchEvent(override val time: Int, val match: Match, val state: MatchState): Track.Event {
    override val bodyStart = state == MatchState.FIRED
}

data class FactEvent(override val time: Int, val isInsert: Boolean, val maintain: Boolean, val producer: Track.Period<*>?): Track.Event {
    override val bodyStart = isInsert
}

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

fun configureTextArea(textArea: JTextArea): JTextArea {
    textArea.isEditable = false
    textArea.lineWrap = true
    textArea.border = BorderFactory.createEtchedBorder()
    textArea.minimumSize = Dimension(0,0)

    return textArea
}