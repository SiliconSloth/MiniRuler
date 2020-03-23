package siliconsloth.miniruler

import com.mojang.ld22.Game
import siliconsloth.miniruler.engine.RuleEngine
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.min

class Visualizer(val engine: RuleEngine): JPanel() {
    val memories = mutableSetOf<Memory>()
    val targets = mutableSetOf<MoveTarget>()
    val stationaries = mutableSetOf<StationaryItem>()

    init {
        preferredSize = Dimension(Game.WIDTH * 3, Game.HEIGHT * 3)

        engine.rule {
            find<CameraLocation>()
            fire {
                repaint()
            }
        }

        engine.rule {
            val memory by find<Memory>()
            fire {
                addMemory(memory, memories)
            }
            end {
                removeMemory(memory, memories)
            }
        }

        engine.rule {
            val target by find<MoveTarget>()
            fire {
                addMemory(target, targets)
            }
            end {
                removeMemory(target, targets)
            }
        }

        engine.rule {
            val camera by find<CameraLocation>()
            val stat by find<StationaryItem> { camera.frame - since > 20 }
            fire {
                addMemory(stat, stationaries)
            }
            end {
                removeMemory(stat, stationaries)
            }
        }
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

        synchronized(this) {
            if (memories.isEmpty()) {
                return
            }

            val minX = memories.map { it.pos.x }.min()!!
            val maxX = memories.map { it.pos.x }.max()!!
            val minY = memories.map { it.pos.y }.min()!!
            val maxY = memories.map { it.pos.y }.max()!!

            val scale = min(width.toDouble() / (maxX - minX).toDouble(), height.toDouble() / (maxY - minY).toDouble())
            g2d.scale(scale, scale)
            g2d.translate(-minX, -minY)

            memories.forEach {
                if (it.entity.ordinal < 23) {
                    // Generate arbitrary colours from tile enum.
                    g2d.color = Color((it.entity.ordinal * 31 + 76) % 256, (it.entity.ordinal * 131 + 176) % 256, (it.entity.ordinal * 231 + 276) % 256, 255)
                    g2d.fillRect(it.pos.x - 8, it.pos.y - 8, 16, 16)
                } else {
                    val ord = it.entity.ordinal - 23
                    g2d.color = Color((ord * 163 + 87) % 256, (ord * 3 + 90) % 256, (ord * 321 + 54) % 256, 255)
                    g2d.fillRect(it.pos.x - 6, it.pos.y - 6, 12, 12)
                }
            }

            targets.forEach {
                g2d.color = Color(0, 255, 0, 255)
                g2d.fillRect(it.target.pos.x - 5, it.target.pos.y - 5, 10, 10)
            }

            stationaries.forEach {
                g2d.color = Color(0, 255, 255, 255)
                g2d.fillRect(it.item.pos.x - 2, it.item.pos.y - 2, 4, 4)
            }
        }
    }

    private fun <T> addMemory(memory: T, memories: MutableSet<T>) {
        synchronized(this) {
            memories.add(memory)
        }
    }

    private fun <T> removeMemory(memory: T, memories: MutableSet<T>) {
        synchronized(this) {
            memories.remove(memory)
        }
    }
}