package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import kotlin.math.*

fun RuleEngine.navigationRules() {
    atomic {
        Direction.values().forEach {
            insert(MoveDesire(it, 0f))
        }
    }

    rule {
        val memory by find<TileMemory> { tile == Tile.TREE }
        val player by find<EntityMemory> { entity == Entity.PLAYER }

        fire {
            val xDiff = memory.x + 8 - (player.x + 4)
            val yDiff = memory.y + 8 - (player.y + 3)

            val mag = sqrt((xDiff*xDiff + yDiff*yDiff).toFloat())
            val strength = 0.99f.pow(mag)

            val angle = ((atan2(yDiff.toFloat(), xDiff.toFloat())/PI.toFloat() + 1) % 2) * 4
            val sector = angle.toInt()
            val interpolation = angle - sector

            val upperStrength = interpolation * strength
            val lowerStrength = (1-interpolation) * strength

            val lowerDirection = sectorToDirection(sector)
            val upperDirection = sectorToDirection((sector + 1) % 8)

            atomic {
                maintain(MoveProposal(lowerDirection, lowerStrength, memory))
                maintain(MoveProposal(upperDirection, upperStrength, memory))
            }
        }
    }

    rule {
        val strengths = Direction.values().map { it to 0f }.toMap().toMutableMap()
        val proposal by find<MoveProposal>()

        fire {
            val dir = proposal.direction

            val oldStrength = strengths[dir]!!
            val newStrength = oldStrength + proposal.strength
            strengths[dir] = newStrength

            replace(MoveDesire(dir, oldStrength), MoveDesire(dir, newStrength))
        }

        end {
            val dir = proposal.direction

            val oldStrength = strengths[dir]!!
            val newStrength = oldStrength - proposal.strength
            strengths[dir] = newStrength

            replace(MoveDesire(dir, oldStrength), MoveDesire(dir, newStrength))
        }
    }

    rule {
        val upPress = KeyPress(Key.UP)
        val downPress = KeyPress(Key.DOWN)
        val leftPress = KeyPress(Key.LEFT)
        val rightPress = KeyPress(Key.RIGHT)

        val desire by find<MoveDesire>()
        not<MoveDesire> { strength > desire.strength }

        fire {
            val presses = mutableMapOf(upPress to false, downPress to false, leftPress to false, rightPress to false)
            when (desire.direction) {
                Direction.UP -> {
                    presses[upPress] = true
                }
                Direction.UP_RIGHT -> {
                    presses[upPress] = true
                    presses[rightPress] = true
                }
                Direction.RIGHT -> {
                    presses[rightPress] = true
                }
                Direction.DOWN_RIGHT -> {
                    presses[downPress] = true
                    presses[rightPress] = true
                }
                Direction.DOWN -> {
                    presses[downPress] = true
                }
                Direction.DOWN_LEFT -> {
                    presses[downPress] = true
                    presses[leftPress] = true
                }
                Direction.LEFT -> {
                    presses[leftPress] = true
                }
                Direction.UP_LEFT -> {
                    presses[upPress] = true
                    presses[leftPress] = true
                }
            }
            atomic {
                presses.forEach { (key, press) ->
                    if (press) {
                        insert(key)
                    } else {
                        delete(key)
                    }
                }
            }
        }
    }
}

fun sectorToDirection(sector: Int): Direction = when (sector) {
    0 -> Direction.LEFT
    1 -> Direction.UP_LEFT
    2 -> Direction.UP
    3 -> Direction.UP_RIGHT
    4 -> Direction.RIGHT
    5 -> Direction.DOWN_RIGHT
    6 -> Direction.DOWN
    7 -> Direction.DOWN_LEFT
    else -> throw RuntimeException("Bad sector $sector")
}