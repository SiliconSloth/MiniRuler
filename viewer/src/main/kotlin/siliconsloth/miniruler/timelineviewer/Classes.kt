package siliconsloth.miniruler.timelineviewer

enum class MatchState {
    MATCHED, FIRED, DROPPED, ENDED;
}

data class Match(val rule: String, val bindings: List<String>)
data class MatchEvent(val match: Match, val state: MatchState, val time: Int)
data class FactEvent(val isInsert: Boolean, val maintain: Boolean, val producer: Match?, val time: Int)