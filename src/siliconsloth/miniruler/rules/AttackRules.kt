package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.attackRules() {
    rule {
        val player by find<EntityMemory> { entity == Entity.PLAYER }
        find<MoveTarget> { aimingAt(player, tile) }
        find<StaminaLevel> { stamina > 8 }

        fire {
            maintain(KeyPress(Key.ATTACK))
        }
    }
}

fun aimingAt(actor: EntityMemory, target: TileMemory): Boolean {
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

    return (target.x - actor.x) in minX2..maxX2
            && (target.y - actor.y) in minY2..maxY2
}