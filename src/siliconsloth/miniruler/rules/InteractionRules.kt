package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.filters.EqualityFilter
import siliconsloth.miniruler.math.Box
import siliconsloth.miniruler.math.Vector
import kotlin.math.abs

fun RuleEngine.attackRules() {
    // If the player is aiming at a tree and has sufficient stamina, attack it.
    rule {
        find<CurrentAction> { action == CHOP_TREES }

        val player by find<Memory> { entity == Entity.PLAYER }
        find<MoveTarget> { target.entity == Entity.TREE && aimingAt(player, target) }
        find<StaminaLevel> { stamina > 8 }

        fire {
            maintain(KeyPress(Key.ATTACK))
        }
    }

    // If the player is standing on top of a item it is trying to collect, keep moving in place
    // until the item is collected. Minicraft only allows items to be picked up while moving.
    rule {
        val upPress = KeyPress(Key.UP)
        val downPress = KeyPress(Key.DOWN)

        find<CurrentAction> { action == CHOP_TREES }

        val player by find<Memory> { entity == Entity.PLAYER }
        find<MoveTarget> { target.entity == Entity.ITEM
                && abs(player.pos.x - target.pos.x) <= 1 && abs(player.pos.y - target.pos.y) <= 1 }

        fire {
            if (exists(EqualityFilter { downPress })) {
                replace(downPress, upPress)
            } else {
                replace(upPress, downPress)
            }
        }
    }

    // When trying to place a workbench, turn to point in a direction where there is empty space in front of the player
    // then place the workbench.
    rule {
        find<CurrentAction> { action == PLACE_WORKBENCH }
        val player by find<Memory> { entity == Entity.PLAYER }
        val obstacles by all<Memory>(AreaFilter { Box(player.pos, player.pos, padding=30) })

        fire {
            val obstructed = obstacles.any { it.entity.solid && aimingAt(player, it) }
            if (obstructed) {
                // If there is an obstacle in front of the player, find a direction in which there is no obstacle.
                Direction.values().forEach { dir ->
                    // Rotate the player and check for obstacles again.
                    val rotated = Memory(player.entity, player.pos, dir, player.item)
                    val obs = obstacles.any { it.entity.solid && aimingAt(rotated, it) }

                    if (!obs) {
                        maintain(KeyPress(Key.fromDirection(dir)))
                        return@forEach
                    }
                }
            } else {
                maintain(KeyPress(Key.ATTACK))
            }
        }
    }
}

fun aimingAt(actor: Memory, target: Spatial): Boolean =
        target.pos in aimBox(actor)

fun aimBox(actor: Memory): Box {
    // Bounding box that target must lie in if the actor is facing down.
    val minX = -6
    val maxX = 6
    val minY = 2
    val maxY = 24

    // Rotate according to direction.
    val minX2 = when (actor.facing) {
        Direction.UP -> -maxX
        Direction.DOWN -> minX
        Direction.LEFT -> -maxY
        Direction.RIGHT -> minY
    }
    val maxX2 = when (actor.facing) {
        Direction.UP -> -minX
        Direction.DOWN -> maxX
        Direction.LEFT -> -minY
        Direction.RIGHT -> maxY
    }
    val minY2 = when (actor.facing) {
        Direction.UP -> -maxY
        Direction.DOWN -> minY
        Direction.LEFT -> minX
        Direction.RIGHT -> -maxX
    }
    val maxY2 = when (actor.facing) {
        Direction.UP -> -minY
        Direction.DOWN -> maxY
        Direction.LEFT -> maxX
        Direction.RIGHT -> -minX
    }

    return Box(Vector(minX2, minY2) + actor.pos, Vector(maxX2, maxY2) + actor.pos)
}