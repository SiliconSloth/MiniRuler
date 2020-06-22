package siliconsloth.miniruler.timelineviewer

data class Track(val name: String, val factClass: String) {
    data class Period(val track: Track, val eventTimes: MutableList<Int>) {
        val start: Int
        get() = eventTimes[0]

        val end: Int
        get() = eventTimes.last()
    }

    val periods = mutableListOf(Period(this, mutableListOf()))

    val hue = (factClass.hashCode() * 17 % 1000) / 1000f

    fun addEvent(event: FactEvent) {
        periods.last().eventTimes.add(event.time)
    }

    fun closePeriod() {
        val times = periods.last().eventTimes
        if (times.size < 2 || times[0] == times.last()) {
            periods.removeAt(periods.size-1)
        }

        periods.add(Period(this, mutableListOf()))
    }

    fun finalize(maxTime: Int) {
        val times = periods.last().eventTimes
        if (times.isEmpty() || times[0] == maxTime) {
            periods.removeAt(periods.size-1)
        } else {
            times.add(maxTime)
        }
    }
}