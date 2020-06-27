package siliconsloth.miniruler.timelineviewer

abstract class Track<T: Track.Owner, E: Track.Event>(val owner: T) {
    interface Event {
        val time: Int
    }

    interface Owner {
        val name: String
        val label: String
        val hue: Float
    }

    abstract val bindingsTitle: String
    abstract val insertsTitle: String
    abstract val maintainsTitle: String
    abstract val deletesTitle: String

    data class Period<E: Event>(val track: Track<*,E>, val events: MutableList<E> = mutableListOf<E>()) {
        var closed: Boolean = false

        val start: Int
        get() = events[0].time

        val end: Int?
        get() = if (closed) events.last().time else null

        val bindings = mutableListOf<InfoListing>()
        val inserts = mutableListOf<InfoListing>()
        val maintains = mutableListOf<InfoListing>()
        val deletes = mutableListOf<InfoListing>()
    }

    val periods = mutableListOf(Period(this))

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

        periods.add(Period(this))
    }

    fun lastPeriod(): Period<E> =
            periods.last { it.events.isNotEmpty() }

    fun finalize(maxTime: Int) {
        val events = periods.last().events
        if (events.isEmpty() || events[0].time == maxTime) {
            periods.removeAt(periods.size-1)
        }
    }
}