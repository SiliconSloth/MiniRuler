package siliconsloth.miniruler.timelineviewer

import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class TimelinePane(val allTracks: List<Track<*,*>>, val maxTime: Int): JPanel(), Scrollable, MouseListener, MouseMotionListener {
    interface SelectionListener {
        fun periodSelected(period: Track.Period<*>?)
    }

    val selectionListeners = mutableListOf<SelectionListener>()
    lateinit var scrollPane: JScrollPane

    /**
     * Tracks that are not hidden by the current filter.
     */
    var visibleTracks = allTracks

    val defaultScale = 20
    val defaultViewportSize = Dimension(1600, 900)

    var mouseOverTrack: Track<*,*>? = null
    var mouseOverPeriod: Track.Period<*>? = null
    var selectedPeriod: Track.Period<*>? = null

    init {
        // Make sure the timeline pane is no smaller than the scroll viewport.
        preferredSize = Dimension(max(maxTime * defaultScale, defaultViewportSize.width),
                max(visibleTracks.size * defaultScale, defaultViewportSize.height))
    }

    fun addSelectionListener(listener: SelectionListener) {
        selectionListeners.add(listener)
    }

    fun selectPeriod(period: Track.Period<*>?) {
        selectedPeriod = period
        selectedPeriod?.track?.let { scrollToTrack(it) }
        selectionListeners.forEach { it.periodSelected(selectedPeriod) }
        repaint()
    }

    fun updateFilter(query: String) {
        val oldTrackCount = visibleTracks.size
        visibleTracks = allTracks.filter { it.label.contains(query) }

        val newHeight = if (oldTrackCount == 0) {
            visibleTracks.size * defaultScale
        } else {
            // Try to retain the current scaling.
            height * visibleTracks.size / oldTrackCount
        }
        preferredSize = Dimension(width, max(parent.height, newHeight))
        size = preferredSize
        scrollPane.verticalScrollBar.maximum = height

        selectedPeriod?.track?.let { scrollToTrack(it) }

        repaint()
    }

    /**
     * If the specified track is off-screen, but not hidden by the filter, scroll such that it is in the centre
     * of the viewport.
     */
    fun scrollToTrack(track: Track<*,*>) {
        val minTrack = trackAt(visibleRect.minY.toInt())
        val maxTrack = min(trackAt(visibleRect.maxY.toInt()), visibleTracks.size - 1)

        val ind = visibleTracks.indexOf(track)
        if (ind != -1 && ind !in minTrack..maxTrack) {
            val center = (ind.toFloat() + 0.5f) * height.toFloat() / visibleTracks.size.toFloat() - parent.height.toFloat() / 2
            scrollPane.verticalScrollBar.value = center.toInt()
        }
    }

    override fun paintComponent(g: Graphics) {
        val g2d = g as Graphics2D
        val xScale = width.toFloat() / maxTime
        val yScale = height.toFloat() / visibleTracks.size

        updateMouseOver()

        g2d.clearRect(visibleRect.x, visibleRect.y, visibleRect.width, visibleRect.height)

        val minTrack = trackAt(visibleRect.minY.toInt())
        val maxTrack = min(trackAt(visibleRect.maxY.toInt()), visibleTracks.size - 1)

        paintGridlines(g2d, minTrack, maxTrack, xScale, yScale)

        val h = ceil(yScale).toInt()
        // Tracks at the top of the screen are painted on top of those lower down,
        // because any overlap looks a little nicer that way.
        for (i in maxTrack.downTo(minTrack)) {
            val track = visibleTracks[i]
            val y = (i * yScale).toInt()

            for (period in track.periods) {
                val x = (period.start * xScale).toInt()
                val w = (((period.end ?: maxTime) - period.start) * xScale).toInt()

                val sat = when (period) {
                    selectedPeriod -> 1f
                    mouseOverPeriod -> 0.3f
                    else -> 0.4f
                }

                if (period.bodyStart != null) {
                    val bx = (period.bodyStart!! * xScale).toInt()
                    g2d.color = Color.getHSBColor(track.hue, sat, 1f)
                    g2d.fillRect(bx, y, w + x - bx, h)
                }

                g2d.color = Color.getHSBColor(track.hue, 0.8f, 1f)
                g2d.drawRect(x, y, w, h)

                for (event in period.events) {
                    val x2 = (event.time * xScale).toInt()
                    g2d.drawLine(x2, y, x2, y + h)
                }
            }

            // Only show track labels if they won't be squashed together.
            if (yScale > g2d.fontMetrics.ascent * 0.6f) {
                val label = if (track.label.length <= 80 || track == selectedPeriod?.track || track == mouseOverTrack) {
                    track.label
                } else {
                    track.label.substring(0, 80) + "..."
                }

                g2d.color = Color.BLACK
                g2d.drawString(label, visibleRect.x, y + g2d.fontMetrics.ascent)
            }
        }
    }

    fun paintGridlines(g2d: Graphics2D, minTrack: Int, maxTrack: Int, xScale: Float, yScale: Float) {
        g2d.color = Color(240, 240, 240)
        for (i in minTrack..maxTrack) {
            val y = ((i + 1) * yScale).toInt()
            g2d.drawLine(visibleRect.x, y, visibleRect.x + visibleRect.width, y)
        }

        for (i in 1 until maxTime) {
            val x = (i * xScale).toInt()
            g2d.drawLine(x, visibleRect.y, x, visibleRect.y + visibleRect.height)
        }
    }

    fun updateMouseOver() {
        val mouse = mousePosition()
        if (visibleTracks.isNotEmpty() && visibleRect.contains(mouse)) {
            val track = trackAt(mouse.y)
            val time = mouse.x * maxTime / width

            mouseOverTrack = visibleTracks[track]
            mouseOverPeriod = mouseOverTrack!!.periods.firstOrNull {
                it.start <= time && it.end?.let { e -> time < e } != false
            }
        } else {
            mouseOverTrack = null
            mouseOverPeriod = null
        }
    }

    fun trackAt(y: Int): Int =
            ((y.toFloat() / height) * visibleTracks.size).toInt()

    fun mousePosition(): Point {
        val ms = MouseInfo.getPointerInfo().location
        val cs = locationOnScreen
        return Point(ms.x - cs.x, ms.y - cs.y)
    }

    override fun getScrollableTracksViewportWidth() = false

    override fun getScrollableTracksViewportHeight() = false

    override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int =
            scrollIncrement(visibleRect, orientation, direction, 10, false)

    override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int =
            scrollIncrement(visibleRect, orientation, direction, 1, true)

    /**
     * Computes the increment required to scroll blockSize grid cells along the specified axis.
     * If exposeFull is true. this will be increased to ensure that an entire new block of cells is exposed.
     */
    fun scrollIncrement(visibleRect: Rectangle, orientation: Int, direction: Int, blockSize: Int, exposeFull: Boolean): Int {
        val horizontal = orientation == SwingConstants.HORIZONTAL
        val unitSize = (if (horizontal) width.toFloat() / maxTime else height.toFloat() / visibleTracks.size) * blockSize
        val pos = if (horizontal) visibleRect.x else visibleRect.y

        val relPos = pos % unitSize
        var dist = if (direction < 0) relPos else unitSize - relPos
        // Ensure that at least one more pixel is exposed.
        if (dist < 1 || (exposeFull && dist < unitSize)) {
            dist += unitSize
        }
        return dist.toInt()
    }

    override fun getPreferredScrollableViewportSize(): Dimension =
            defaultViewportSize

    override fun mouseMoved(e: MouseEvent) {
        repaint()
    }

    override fun mousePressed(e: MouseEvent) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            selectPeriod(mouseOverPeriod)
        }
    }

    override fun mouseDragged(e: MouseEvent) {
    }

    override fun mouseEntered(e: MouseEvent) {
    }

    override fun mouseClicked(e: MouseEvent) {
    }

    override fun mouseExited(e: MouseEvent) {
    }

    override fun mouseReleased(e: MouseEvent) {
    }
}