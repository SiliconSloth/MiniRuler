package siliconsloth.miniruler.engine.recorder

import siliconsloth.miniruler.math.Vector
import java.awt.Dimension
import java.awt.Rectangle
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.Scrollable
import javax.swing.SwingConstants

class TimelinePane(tracks: List<Track>, val maxTime: Int): JPanel(), Scrollable {

    val scale = Vector(10, 10)

    val trackPanels = tracks.map { TrackPanel(it, maxTime, scale).also { add(it) } }

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
    }

    override fun getScrollableTracksViewportWidth() = false

    override fun getScrollableTracksViewportHeight() = false

    override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int =
            scrollIncrement(visibleRect, orientation, direction, 10, false)

    override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int =
            scrollIncrement(visibleRect, orientation, direction, 1, true)

    fun scrollIncrement(visibleRect: Rectangle, orientation: Int, direction: Int, blockSize: Int, exposeFull: Boolean): Int {
        val horizontal = orientation == SwingConstants.HORIZONTAL
        val unitSize = (if (horizontal) scale.x else scale.y) * blockSize
        val pos = if (horizontal) visibleRect.x else visibleRect.y

        val relPos = pos % unitSize
        var dist = if (direction < 0) relPos else unitSize - relPos
        if (dist == 0 || (exposeFull && dist < unitSize)) {
            dist += unitSize
        }
        return dist
    }

    override fun getPreferredScrollableViewportSize(): Dimension =
            Dimension(1800, 900)
}