package siliconsloth.miniruler.engine.recorder

import siliconsloth.miniruler.math.Vector
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JPanel

class TrackPanel(val track: Track, maxTime: Int, val scale: Vector): JPanel() {
    init {
        background = Color(track.name.hashCode())
        preferredSize = Dimension(maxTime * scale.x, scale.y)
    }

    override fun paintComponent(g: Graphics) {
        val g2d = g as Graphics2D

        g2d.clearRect(0, 0, width, height)

        g2d.color = Color.MAGENTA
        for (period in track.periods) {
            g2d.fillRect(period.start * scale.x, 0, (period.end - period.start) * scale.x, height)
        }

        g2d.color = Color.BLACK
        g2d.drawString(track.name, 0, height)
    }
}