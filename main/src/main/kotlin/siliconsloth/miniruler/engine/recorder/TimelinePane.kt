package siliconsloth.miniruler.engine.recorder

import siliconsloth.miniruler.math.Vector
import java.awt.Dimension
import java.awt.Rectangle
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.Scrollable

class TimelinePane(tracks: List<Track>, val maxTime: Int): JPanel(), Scrollable {

    val scale = Vector(10, 10)

    val trackPanels = tracks.map { TrackPanel(it, maxTime, scale).also { add(it) } }

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
    }

    override fun getScrollableTracksViewportWidth() = false

    override fun getScrollableTracksViewportHeight() = false

    override fun getScrollableBlockIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int) = 5

    override fun getScrollableUnitIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int) = 5

    override fun getPreferredScrollableViewportSize(): Dimension =
            Dimension(1800, 900)
}