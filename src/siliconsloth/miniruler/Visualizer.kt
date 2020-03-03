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

class Visualizer(engine: RuleEngine): JPanel() {
    val tileMemories = mutableSetOf<TileMemory>()
    val entityMemories = mutableSetOf<EntityMemory>()

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

//        g2d.scale(3.0, 3.0)

        synchronized(this) {
//            println("Tiles: " + tileMemories.size)
//            println("Entities: " + entityMemories.size)
//            val mems = spatialMemoryStore.loadedMemories
            val mems: Set<Spatial> = (tileMemories.union(entityMemories))
            mems.map { it.x }.min()?.let { minX ->
            mems.map { it.x }.max()?.let { maxX ->
            mems.map { it.y }.min()?.let { minY ->
            mems.map { it.y }.max()?.let { maxY ->
                val scale = min(width.toDouble() / (maxX - minX).toDouble(), height.toDouble() / (maxY - minY).toDouble())
                g2d.scale(scale, scale)
                g2d.translate(-minX, -minY)

    //            mems.filter { it is TileMemory }.forEach {
                tileMemories.forEach {
    //                if (!(it is TileMemory)) return
                    // Generate arbitrary colours from tile enum.
                    val color = Color((it.tile.ordinal * 31 + 76) % 256, (it.tile.ordinal * 131 + 176) % 256, (it.tile.ordinal * 231 + 276) % 256, 255)
                    val freshness = 1 //(it.frame - minF + 1).toDouble() / (maxF - minF + 1).toDouble()
                    g2d.color = Color((color.red * freshness).toInt(), (color.green * freshness).toInt(), (color.blue * freshness).toInt(), color.alpha)

                    g2d.fillRect(it.x, it.y, 16, 16)
                }

    //            mems.filter { it is EntityMemory }.forEach {
                entityMemories.forEach {
    //                if (!(it is EntityMemory)) return
                    val color = Color((it.entity.ordinal * 163 + 87) % 256, (it.entity.ordinal * 3 + 90) % 256, (it.entity.ordinal * 321 + 54) % 256, 255)
                    val freshness = 1 //(it.frame - minF + 1).toDouble() / (maxF - minF + 1).toDouble()
                    g2d.color = Color((color.red * freshness).toInt(), (color.green * freshness).toInt(), (color.blue * freshness).toInt(), color.alpha)

                    g2d.fillRect(it.x - 6, it.y - 6, 12, 12)
                }
            }}}}
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