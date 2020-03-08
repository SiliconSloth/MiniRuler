package siliconsloth.miniruler.rules

import com.mojang.ld22.Game
import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import kotlin.math.pow
import kotlin.math.sqrt

fun RuleEngine.navigationRules() {
    atomic {
        insert(KeyDesire(Key.UP, 0f))
        insert(KeyDesire(Key.DOWN, 0f))
        insert(KeyDesire(Key.LEFT, 0f))
        insert(KeyDesire(Key.RIGHT, 0f))
    }

    rule {
        val memory by find<TileMemory> { tile == Tile.TREE }
        val player by find<EntityMemory> { entity == Entity.PLAYER }

        fire {
            val xDiff = memory.x + 8 - (player.x + 4)
            val yDiff = memory.y + 8 - (player.y + 3)

            val mag = sqrt((xDiff*xDiff + yDiff*yDiff).toFloat())
            val x = xDiff / mag
            val y = yDiff / mag

            val strength = 0.99f.pow(mag)

            atomic {
                if (xDiff > 4) {
                    maintain(KeyProposal(Key.RIGHT, x * strength, memory))
                } else if ( xDiff < -4) {
                    maintain(KeyProposal(Key.LEFT, -x * strength, memory))
                } else {
                    maintain(KeyProposal(Key.LEFT, -strength * 10, memory))
                    maintain(KeyProposal(Key.RIGHT, -strength * 10, memory))
                }

                if (yDiff > 4) {
                    maintain(KeyProposal(Key.DOWN, y * strength, memory))
                } else if ( yDiff < -4) {
                    maintain(KeyProposal(Key.UP, -y * strength, memory))
                } else {
                    maintain(KeyProposal(Key.UP, -strength * 10, memory))
                    maintain(KeyProposal(Key.DOWN, -strength * 10, memory))
                }
            }
        }
    }

    rule {
        val strengths = mutableMapOf(Key.UP to 0f, Key.DOWN to 0f, Key.LEFT to 0f, Key.RIGHT to 0f)
        val proposal by find<KeyProposal>()

        fire {
            val key = proposal.key

            val oldStrength = strengths[key]!!
            val newStrength = oldStrength + proposal.strength
            strengths[key] = newStrength
            println(strengths)

            replace(KeyDesire(key, oldStrength), KeyDesire(key, newStrength))
        }

        end {
            val key = proposal.key

            val oldStrength = strengths[key]!!
            val newStrength = oldStrength - proposal.strength
            strengths[key] = newStrength

            replace(KeyDesire(key, oldStrength), KeyDesire(key, newStrength))
        }
    }

    rule {
        val upPress = KeyPress(Key.UP)
        val downPress = KeyPress(Key.DOWN)

        val up by find<KeyDesire> { key == Key.UP }
        val down by find<KeyDesire> { key == Key.DOWN }

        fire {
            atomic {
                if (up.strength > down.strength) {
                    replace(downPress, upPress)
                } else if (down.strength > 0) {
                    replace(upPress, downPress)
                } else {
                    delete(upPress)
                    delete(downPress)
                }
            }
        }
    }

    rule {
        val leftPress = KeyPress(Key.LEFT)
        val rightPress = KeyPress(Key.RIGHT)

        val left by find<KeyDesire> { key == Key.LEFT }
        val right by find<KeyDesire> { key == Key.RIGHT }

        fire {
            atomic {
                if (left.strength > right.strength) {
                    replace(rightPress, leftPress)
                } else if (right.strength > 0) {
                    replace(leftPress, rightPress)
                } else {
                    delete(leftPress)
                    delete(rightPress)
                }
            }
        }
    }
}