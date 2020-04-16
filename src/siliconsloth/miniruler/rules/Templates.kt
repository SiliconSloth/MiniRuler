package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.builders.RuleBuilder
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.matching.CompleteMatch
import siliconsloth.miniruler.math.Box

/**
 * Rule that rotates the player until they are not facing any obstacles (as defined by obstacleCondition)
 * and then executes whenClear. Only acts if all the provided bindings are met first.
 */
fun RuleEngine.faceClear(bindings: RuleBuilder.() -> Unit, obstacleCondition: (Memory) -> Boolean,
                         whenClear: CompleteMatch.() -> Unit) = rule {
    bindings()
    val player by find<Memory> { entity == Entity.PLAYER }
    val obstacles by all<Memory>(AreaFilter { Box(player.pos, player.pos, padding=30) })

    fire {
        val obstructed = obstacles.any { obstacleCondition(it) && aimingAt(player, it) }
        if (obstructed) {
            // If there is an obstacle in front of the player, find a direction in which there are no obstacles.
            Direction.values().forEach { dir ->
                // Rotate the player and check for obstacles again.
                val rotated = Memory(player.entity, player.pos, dir, player.item)
                val obs = obstacles.any { obstacleCondition(it) && aimingAt(rotated, it) }

                if (!obs) {
                    maintain(KeyPress(Key.fromDirection(dir)))
                    return@forEach
                }
            }
        } else {
            whenClear()
        }
    }
}