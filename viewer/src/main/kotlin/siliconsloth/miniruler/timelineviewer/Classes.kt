package siliconsloth.miniruler.timelineviewer

enum class MatchState {
    MATCHED, FIRED, DROPPED, ENDED;
}

data class Match(val rule: String, val bindings: List<String>): Track.Owner {
    override val label = "$rule: $bindings"
    override val hue = (rule.hashCode() * 17 % 1000) / 1000f
}

data class Fact(val fact: String, val factClass: String): Track.Owner {
    override val label = fact
    override val hue = (factClass.hashCode() * 17 % 1000) / 1000f
}

data class MatchEvent(override val time: Int, val match: Match, val state: MatchState): Track.Event
data class FactEvent(override val time: Int, val isInsert: Boolean, val maintain: Boolean, val producer: Match?): Track.Event