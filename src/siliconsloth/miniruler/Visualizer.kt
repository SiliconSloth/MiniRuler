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
    val tileMemories = mutableSetOf<TileMemory>()
    val entityMemories = mutableSetOf<EntityMemory>()
    val moveProposals = mutableSetOf<MoveProposal>()
    val moveDesires = mutableSetOf<MoveDesire>()

    init {
        preferredSize = Dimension(Game.WIDTH * 3, Game.HEIGHT * 3)

        engine.rule {
            find<CameraLocation>()
            fire {
                repaint()
            }
        }

        engine.rule {
            val memory by find<TileMemory>()
            fire {
                addMemory(memory, tileMemories)
            }
            end {
                removeMemory(memory, tileMemories)
            }
        }

        engine.rule {
            val memory by find<EntityMemory>()
            fire {
                addMemory(memory, entityMemories)
            }
            end {
                removeMemory(memory, entityMemories)
            }
        }

        engine.rule {
            val proposal by find<MoveProposal>()
            fire {
                addMemory(proposal, moveProposals)
            }
            end {
                removeMemory(proposal, moveProposals)
            }
        }

        engine.rule {
            val desire by find<MoveDesire>()
            fire {
                addMemory(desire, moveDesires)
            }
            end {
                removeMemory(desire, moveDesires)
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
            val mems: Set<Spatial> = (tileMemories.union(entityMemories))
            mems.map { it.x }.min()?.let { minX ->
            mems.map { it.x }.max()?.let { maxX ->
            mems.map { it.y }.min()?.let { minY ->
            mems.map { it.y }.max()?.let { maxY ->
                val scale = min(width.toDouble() / (maxX - minX).toDouble(), height.toDouble() / (maxY - minY).toDouble())
                g2d.scale(scale, scale)
                g2d.translate(-minX, -minY)

                tileMemories.forEach {
                    // Generate arbitrary colours from tile enum.
                    g2d.color = Color((it.tile.ordinal * 31 + 76) % 256, (it.tile.ordinal * 131 + 176) % 256, (it.tile.ordinal * 231 + 276) % 256, 255)
                    g2d.fillRect(it.x, it.y, 16, 16)
                }

                entityMemories.forEach {
                    g2d.color = Color((it.entity.ordinal * 163 + 87) % 256, (it.entity.ordinal * 3 + 90) % 256, (it.entity.ordinal * 321 + 54) % 256, 255)
                    g2d.fillRect(it.x - 6, it.y - 6, 12, 12)

                    if (it.entity == Entity.PLAYER) {
                        moveDesires.forEach { d ->
                            drawDirectional(g2d, it.x - 3, it.y - 3, d.direction, d.strength, d.direction == moveDesires.maxBy { it.strength }!!.direction)
                        }
                    }
                }

                moveProposals.forEach {
                    drawDirectional(g2d, it.cause.x + 3, it.cause.y + 3, it.direction, it.strength, false)
                }
            }}}}
        }
    }

    private fun drawDirectional(g2d: Graphics2D, x: Int, y: Int, direction: Direction, strength: Float, red: Boolean) {
        val brightness = (strength * 500).toInt().coerceIn(0, 255)
        g2d.color = g2d.color.run { Color(if (red) { 255 } else { 0 }, brightness, if (red) { 0 } else { 255 }, 100) }

        val xOff = when (direction) {
            Direction.UP_RIGHT -> 1
            Direction.RIGHT -> 1
            Direction.DOWN_RIGHT -> 1
            Direction.UP_LEFT -> -1
            Direction.LEFT -> -1
            Direction.DOWN_LEFT -> -1
            else -> 0
        }
        val yOff = when (direction) {
            Direction.UP_LEFT -> -1
            Direction.UP -> -1
            Direction.UP_RIGHT -> -1
            Direction.DOWN_LEFT -> 1
            Direction.DOWN -> 1
            Direction.DOWN_RIGHT -> 1
            else -> 0
        }
        g2d.fillRect(x + 2 + xOff*6, y + 2 + yOff*6, 6, 6)
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