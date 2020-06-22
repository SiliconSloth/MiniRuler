package siliconsloth.miniruler.timelineviewer

data class Track(val name: String, val factClass: String) {
    data class Period(val track: Track, val start: Int, val end: Int, val inserter: Match?, val deleter: Match?)

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
                periods.add(Period(this, lastStart!!, event.time, lastInserter, event.producer))
                lastStart = null
            }
        }
    }

    fun finalize(maxTime: Int) {
        if (lastStart != null) {
            periods.add(Period(this, lastStart!!, maxTime, lastInserter, null))
            lastStart = null
        }
    }
}