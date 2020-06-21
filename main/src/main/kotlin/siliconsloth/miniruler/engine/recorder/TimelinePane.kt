package siliconsloth.miniruler.engine.recorder

import siliconsloth.miniruler.math.Vector
import java.awt.*
import javax.swing.JPanel
import javax.swing.Scrollable
import javax.swing.SwingConstants
import kotlin.math.max

class TimelinePane(val tracks: List<Track>, val maxTime: Int): JPanel(), Scrollable {

    val defaultScale = Vector(10, 10)
    val defaultViewportSize = Dimension(1800, 900)

    init {
        preferredSize = Dimension(max(maxTime * defaultScale.x, defaultViewportSize.width),
                max(tracks.size * defaultScale.y, defaultViewportSize.height))
    }

    override fun paintComponent(g: Graphics?) {
        val g2d = g as Graphics2D
        val xScale = width.toFloat() / maxTime
        val yScale = height.toFloat() / tracks.size

        g2d.clearRect(0, 0, width, height)

        for ((i, track) in tracks.withIndex()) {
            g2d.color = Color.MAGENTA
            for (period in track.periods) {
                g2d.fillRect((period.start * xScale).toInt(), (i * yScale).toInt(),
                        ((period.end - period.start) * xScale).toInt(), yScale.toInt())
            }

            g2d.color = Color.BLACK
            g2d.drawString(track.name, 0, ((i + 1) * yScale).toInt())
        }
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
}