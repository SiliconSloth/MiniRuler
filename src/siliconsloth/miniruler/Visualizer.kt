package siliconsloth.miniruler

import com.mojang.ld22.Game
import org.kie.api.event.rule.ObjectDeletedEvent
import org.kie.api.event.rule.ObjectInsertedEvent
import org.kie.api.event.rule.ObjectUpdatedEvent
import org.kie.api.event.rule.RuleRuntimeEventListener
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JFrame
import javax.swing.JPanel

class Visualizer: JPanel(), RuleRuntimeEventListener {
    val sightings = mutableSetOf<TileSighting>()

    init {
        preferredSize = Dimension(Game.WIDTH * 3, Game.HEIGHT * 3)
    }

    fun display() {
        val frame = JFrame("MiniRuler Visualizer")
        frame.add(this)
        frame.pack()
        frame.isVisible = true
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D

        g2d.scale(3.0, 3.0)
        g2d.translate(Game.WIDTH / 2, (Game.HEIGHT - 8) / 2)

        synchronized(this) {
            sightings.forEach {
                // Generate arbitrary colours from tile enum.
                g2d.color = Color((it.tile.ordinal * 31 + 76) % 256, (it.tile.ordinal * 131 + 176) % 256, (it.tile.ordinal * 231 + 276) % 256, 255)
                g2d.fillRect(it.x, it.y, 16, 16)
            }
        }

        // Draw player.
        g2d.color = Color(255, 200, 0, 255)
        g2d.fillRect(-5, -5, 10, 10)
    }

    override fun objectInserted(event: ObjectInsertedEvent) {
        (event.`object` as? TileSighting)?.let {
            synchronized(this) {
                sightings.add(it)
            }
            repaint()
        }
    }

    override fun objectDeleted(event: ObjectDeletedEvent) {
        (event.oldObject as? TileSighting)?.let {
            synchronized(this) {
                sightings.remove(it)
            }
            repaint()
        }
    }

    override fun objectUpdated(event: ObjectUpdatedEvent) {
        (event.oldObject as? TileSighting)?.let {
            synchronized(this) {
                sightings.remove(it)
            }
            repaint()
        }

        (event.`object` as? TileSighting)?.let {
            synchronized(this) {
                sightings.add(it)
            }
            repaint()
        }
    }
}