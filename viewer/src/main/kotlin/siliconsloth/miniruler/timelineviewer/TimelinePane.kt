package siliconsloth.miniruler.timelineviewer

import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.JPanel
import javax.swing.Scrollable
import javax.swing.SwingConstants
import kotlin.math.max

class TimelinePane(val tracks: List<Track>, val maxTime: Int): JPanel(), Scrollable, MouseListener, MouseMotionListener {

    val defaultScale = 10
    val defaultViewportSize = Dimension(1800, 900)

    var mouseOverPeriod: Track.Period? = null
    var selectedPeriod: Track.Period? = null

    init {
        preferredSize = Dimension(max(maxTime * defaultScale, defaultViewportSize.width),
                max(tracks.size * defaultScale, defaultViewportSize.height))
    }

    override fun paintComponent(g: Graphics?) {
        val g2d = g as Graphics2D
        val xScale = width.toFloat() / maxTime
        val yScale = height.toFloat() / tracks.size

        updateMouseOverPeriod()

        g2d.clearRect(0, 0, width, height)

        for ((i, track) in tracks.withIndex()) {
            for (period in track.periods) {
                g2d.color = when (period) {
                    selectedPeriod -> Color.GREEN
                    mouseOverPeriod -> Color.CYAN
                    else -> Color.MAGENTA
                }
                g2d.fillRect((period.start * xScale).toInt(), (i * yScale).toInt(),
                        ((period.end - period.start) * xScale).toInt(), yScale.toInt())
            }

            g2d.color = Color.BLACK
            g2d.drawString(track.name, visibleRect.x, ((i + 1) * yScale).toInt())
        }
    }

    fun updateMouseOverPeriod() {
        val mouse = mousePosition()
        if (visibleRect.contains(mouse)) {
            val track = mouse.y * tracks.size / height
            val time = mouse.x * maxTime / width

            mouseOverPeriod = tracks[track].periods.firstOrNull {
                it.start <= time && time <= it.end
            }
        } else {
            mouseOverPeriod = null
        }
    }

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