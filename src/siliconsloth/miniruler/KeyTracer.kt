package siliconsloth.miniruler

import com.mojang.ld22.Game
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.math.Box
import siliconsloth.miniruler.math.Vector
import siliconsloth.miniruler.pathfinder.PathFinder
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.min

class KeyTracer(val engine: RuleEngine): JPanel() {
    data class KeyEvent(val down: Boolean, val time: Int)

    val eventTimes = Key.values().map { it to mutableListOf<KeyEvent>() }.toMap()
    var time = 0

    init {
        preferredSize = Dimension(800, 100)

        engine.rule {
            val press by find<KeyPress>()

            fire {
                synchronized(this@KeyTracer) {
                    eventTimes[press.key]!!.add(KeyEvent(true, time))
                }
                time++
                repaint()
            }

            end {
                synchronized(this@KeyTracer) {
                    eventTimes[press.key]!!.add(KeyEvent(false, time))
                }
                time++
                repaint()
            }
        }
    }

    fun display() {
        val frame = JFrame("MiniRuler Key Tracer")
        frame.add(this)
        frame.pack()
        frame.isVisible = true
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D

        synchronized(this) {
            if (eventTimes.any { it.value.isNotEmpty() }) {
                eventTimes.values.forEach { it.retainAll { it.time > time - 50 } }

                val minTime = eventTimes.values.filter { it.isNotEmpty() }.map { it[0].time }.min()!!
                val maxTime = time

                val barHeight = height / eventTimes.size
                val scale = width / (maxTime - minTime).toFloat()

                g2d.color = Color.RED
                eventTimes.forEach { (key, events) ->
                    var start = minTime
                    var count = 0
                    events.forEach {
                        if (it.down) {
                            if (count == 0) {
                                start = it.time
                            }
                            count++
                        } else {
                            count--
                            if (count < 0) {
                                count = 0
                                start = minTime
                            }
                            if (count == 0) {
                                g2d.fillRect(((start - minTime) * scale).toInt(), key.ordinal * barHeight,
                                        ((it.time - start) * scale).toInt(), barHeight)
                            }
                        }
                    }
                    if (count > 0) {
                        g2d.fillRect(((start - minTime) * scale).toInt(), key.ordinal * barHeight,
                                ((maxTime - start) * scale).toInt(), barHeight)
                    }
                }
            }
        }
    }
}