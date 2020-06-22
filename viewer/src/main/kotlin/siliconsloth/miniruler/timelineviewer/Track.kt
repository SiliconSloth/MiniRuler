package siliconsloth.miniruler.timelineviewer

data class Track<E: Track.Event>(val name: String, val factClass: String) {
    interface Event {
        val time: Int
    }

    data class Period<E: Event>(val track: Track<*>, val events: MutableList<E>, var closed: Boolean = false) {
        val start: Int
        get() = events[0].time

        val end: Int?
        get() = if (closed) events.last().time else null
    }

    val periods = mutableListOf(Period<E>(this, mutableListOf()))

    val hue = (factClass.hashCode() * 17 % 1000) / 1000f

    fun addEvent(event: E) {
        periods.last().events.add(event)
    }

    fun closePeriod() {
        val events = periods.last().events
        if (events.size < 2 || events[0].time == events.last().time) {
            periods.removeAt(periods.size-1)
        } else {
            periods.last().closed = true
        }

        periods.add(Period(this, mutableListOf()))
    }

    fun finalize(maxTime: Int) {
        val events = periods.last().events
        if (events.isEmpty() || events[0].time == maxTime) {
            periods.removeAt(periods.size-1)
        }
    }
}