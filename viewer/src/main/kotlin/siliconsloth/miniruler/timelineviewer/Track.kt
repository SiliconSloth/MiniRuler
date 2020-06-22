package siliconsloth.miniruler.timelineviewer

data class Track<T: Track.Owner, E: Track.Event>(val owner: T) {
    interface Event {
        val time: Int
    }

    interface Owner {
        val label: String
        val hue: Float
    }

    data class Period<E: Event>(val track: Track<*,E>, val events: MutableList<E>, var closed: Boolean = false) {
        val start: Int
        get() = events[0].time

        val end: Int?
        get() = if (closed) events.last().time else null
    }

    val periods = mutableListOf(Period<E>(this, mutableListOf()))

    val label: String
    get() = owner.label

    val hue: Float
    get() = owner.hue

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