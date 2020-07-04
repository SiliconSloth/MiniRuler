package siliconsloth.miniruler.timelineviewer

abstract class Track<T: Track.Owner, E: Track.Event>(val owner: T) {
    interface Event {
        val time: Int

        /**
         * Whether this event can indicate the start of a Period's main body.
         */
        val bodyStart: Boolean
    }

    interface Owner {
        /**
         * Short text representation shown in the info panel.
         */
        val name: String

        /**
         * Long text representation shown on track labels.
         */
        val label: String

        val hue: Float
    }

    // Titles of the four sections on the info panel.
    abstract val bindingsTitle: String
    abstract val insertsTitle: String
    abstract val maintainsTitle: String
    abstract val deletesTitle: String
    abstract val triggersTitle: String

    data class Period<E: Event>(val track: Track<*,E>, val events: MutableList<E> = mutableListOf<E>()) {
        /**
         * Whether this period has an end. If false it continues to the end of the timeline.
         */
        var closed: Boolean = false

        val start: Int
        get() = events[0].time

        val bodyStart: Int?
        get() = events.firstOrNull { it.bodyStart }?.time

        val end: Int?
        get() = if (closed) events.last().time else null

        /**
         * For matches: Fact periods that were bind values for this match.
         * Facts have no bindings.
         */
        val bindings = mutableListOf<InfoListing>()
        // Facts inserted/deleted by this match or matches that inserted/deleted this fact.
        val inserts = mutableListOf<InfoListing>()
        val maintains = mutableListOf<InfoListing>()
        val deletes = mutableListOf<InfoListing>()
        /**
         * For facts: Matches that matched with this fact as a bind value.
         * Matches have no triggers.
         */
        val triggers = mutableListOf<InfoListing>()
    }

    val periods = mutableListOf(Period(this))

    val label: String
    get() = owner.label

    val hue: Float
    get() = owner.hue

    fun addEvent(event: E) {
        periods.last().events.add(event)
    }

    /**
     * Close the last period and starts a new one to add future events to.
     * If the last period had one or fewer events, it is degenerate and is removed
     * rather than being closed.
     */
    fun closePeriod() {
        val events = periods.last().events
        if (events.size < 2 || events[0].time == events.last().time) {
            periods.removeAt(periods.size-1)
        } else {
            periods.last().closed = true
        }

        periods.add(Period(this))
    }

    /**
     * Get the last non-empty period.
     */
    fun lastPeriod(): Period<E> =
            periods.last { it.events.isNotEmpty() }

    /**
     * Remove a degenerate period from the end of the track, if present.
     */
    fun finalize(maxTime: Int) {
        val events = periods.last().events
        if (events.isEmpty() || events[0].time == maxTime) {
            periods.removeAt(periods.size-1)
        }
    }
}