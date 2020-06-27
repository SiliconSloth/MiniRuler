package siliconsloth.miniruler.timelineviewer

interface InfoListing {
    val listing: List<Pair<String, Track.Period<*>?>>
}

class SingletonListing(val period: Track.Period<*>): InfoListing {
    override val listing = listOf(period.track.owner.name to period)

    override fun toString() = period.track.owner.name
}

class EmptyListing: InfoListing {
    override val listing = listOf("N/A" to null)

    override fun toString() = "null"
}

class MultiListing(val periods: List<Track.Period<*>>): InfoListing {
    override val listing = when (periods.size) {
        0 -> listOf("[]" to null)
        1 -> listOf("[${periods[0].track.label}]" to periods[0])
        else -> listOf("[${periods[0].track.label},\n" to periods[0]) +
                periods.subList(1, periods.size - 1).map { "${it.track.label},\n" to it } +
                listOf("${periods.last().track.label}]" to periods.last())
    }

    override fun toString() = periods.map { it.track.owner.name }.toString()
}