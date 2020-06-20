package siliconsloth.miniruler.engine.recorder

import siliconsloth.miniruler.engine.matching.CompleteMatch

data class Match(val rule: String, val bindings: List<String>)
data class MatchEvent(val match: Match, val state: CompleteMatch.State, val time: Int)
data class FactEvent(val isInsert: Boolean, val maintain: Boolean, val producer: Match?, val time: Int)