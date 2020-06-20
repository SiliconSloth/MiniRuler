package siliconsloth.miniruler.engine.recorder

class Track(val name: String) {
    data class Period(val start: Int, val end: Int, val inserter: Match?, val deleter: Match?)

    val periods = mutableListOf<Period>()

    var lastStart: Int? = null
    var lastInserter: Match? = null

    fun addEvent(event: FactEvent) {
        if (lastStart == null) {
            if (event.isInsert) {
                lastStart = event.time
                lastInserter = event.producer
            }
        } else {
            if (!event.isInsert) {
                periods.add(Period(lastStart!!, event.time, lastInserter, event.producer))
                lastStart = null
            }
        }
    }

    fun finalize(maxTime: Int) {
        if (lastStart != null) {
            periods.add(Period(lastStart!!, maxTime, lastInserter, null))
            lastStart = null
        }
    }
}