package siliconsloth.miniruler.timelineviewer

import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.JPanel
import javax.swing.Scrollable
import javax.swing.SwingConstants
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class TimelinePane(val tracks: List<Track<*,*>>, val maxTime: Int): JPanel(), Scrollable, MouseListener, MouseMotionListener {

    val defaultScale = 20
    val defaultViewportSize = Dimension(1800, 900)

    var mouseOverTrack: Track<*,*>? = null
    var mouseOverPeriod: Track.Period<*>? = null
    var selectedPeriod: Track.Period<*>? = null

    init {
        preferredSize = Dimension(max(maxTime * defaultScale, defaultViewportSize.width),
                max(tracks.size * defaultScale, defaultViewportSize.height))
    }

    override fun paintComponent(g: Graphics) {
        val g2d = g as Graphics2D
        val xScale = width.toFloat() / maxTime
        val yScale = height.toFloat() / tracks.size

        updateMouseOver()

        g2d.clearRect(visibleRect.x, visibleRect.y, visibleRect.width, visibleRect.height)

        val minTrack = trackAt(visibleRect.minY.toInt())
        val maxTrack = min(trackAt(visibleRect.maxY.toInt()), tracks.size - 1)

        paintGridlines(g2d, minTrack, maxTrack, xScale, yScale)

        val h = ceil(yScale).toInt()
        for (i in maxTrack.downTo(minTrack)) {
            val track = tracks[i]
            val y = (i * yScale).toInt()

            for (period in track.periods) {
                val x = (period.start * xScale).toInt()
                val w = (((period.end ?: maxTime) - period.start) * xScale).toInt()

                val sat = when (period) {
                    selectedPeriod -> 1f
                    mouseOverPeriod -> 0.3f
                    else -> 0.4f
                }

                g2d.color = Color.getHSBColor(track.hue, sat, 1f)
                g2d.fillRect(x, y, w, h)

                g2d.color = Color.getHSBColor(track.hue, 0.8f, 1f)
                g2d.drawRect(x, y, w, h)

                for (event in period.events) {
                    val x2 = (event.time * xScale).toInt()
                    g2d.drawLine(x2, y, x2, y + h)
                }
            }

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
        if (visibleRect.contains(mouse)) {
            val track = trackAt(mouse.y)
            val time = mouse.x * maxTime / width

            mouseOverTrack = tracks[track]
            mouseOverPeriod = mouseOverTrack!!.periods.firstOrNull { it: Track.Period<*> ->
                it.start <= time && it.end?.let { e -> time <= e } != false
            }
        } else {
            mouseOverTrack = null
            mouseOverPeriod = null
        }
    }

    fun trackAt(y: Int): Int =
            y * tracks.size / height

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

    fun scrollIncrement(visibleRect: Rectangle, orientation: Int, direction: Int, blockSize: Int, exposeFull: Boolean): Int {
        val horizontal = orientation == SwingConstants.HORIZONTAL
        val unitSize = (if (horizontal) width.toFloat() / maxTime else height.toFloat() / tracks.size) * blockSize
        val pos = if (horizontal) visibleRect.x else visibleRect.y

        val relPos = pos % unitSize
        var dist = if (direction < 0) relPos else unitSize - relPos
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
        if (e.button == MouseEvent.BUTTON1) {
            selectedPeriod = mouseOverPeriod
        }
        repaint()
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