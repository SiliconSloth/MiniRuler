package siliconsloth.miniruler.timelineviewer

enum class MatchState {
    MATCHED, FIRED, DROPPED, ENDED;
}

data class Match(val rule: String, val bindings: List<String>)
data class MatchEvent(val match: Match, val state: MatchState, val time: Int)
data class FactEvent(override val time: Int, val isInsert: Boolean, val maintain: Boolean, val producer: Match?): Track.Event

data class Fact(val fact: String, val factClass: String): Track.Owner {
    override val label = fact
    override val hue = (factClass.hashCode() * 17 % 1000) / 1000f
}