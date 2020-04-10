package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.EqualityFilter
import kotlin.math.abs

fun RuleEngine.attackRules() {
    rule {
        find<CurrentAction> { action == CHOP_TREES }

        val player by find<Memory> { entity == Entity.PLAYER }
        find<MoveTarget> { target.entity == Entity.TREE && aimingAt(player, target) }
        find<StaminaLevel> { stamina > 8 }

        fire {
            maintain(KeyPress(Key.ATTACK))
        }
    }

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
}

fun aimingAt(actor: Memory, target: Spatial): Boolean {
    // Bounding box that target must lie in if the actor is facing down.
    val minX = -6
    val maxX = 6
    val minY = 0
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

    return (target.pos.x - actor.pos.x) in minX2..maxX2
            && (target.pos.y - actor.pos.y) in minY2..maxY2
}