package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.math.Box
import siliconsloth.miniruler.math.Vector

fun RuleEngine.attackRules() {
    rule {
        find<CurrentAction> { action == CHOP_TREES || action is MineRock || action == DIG_SAND }
        find<StaminaLevel> { stamina > 6 }
        val player by find<Memory> { entity == Entity.PLAYER }
        val target by find<MoveTarget> { target.entity == Entity.TREE || target.entity == Entity.ROCK
                || target.entity == Entity.SAND }

        fire {
            if (aimingAtTile(player, target.target)) {
                if (target.target.entity == Entity.SAND) {
                    maintain(KeyRequest(Key.ATTACK))
                } else {
                    maintain(KeySpam(Key.ATTACK))
                }
            } else {
                Direction.values().forEach { dir ->
                    // Rotate the player and check for obstacles again.
                    val rotated = Memory(player.entity, player.pos, dir, player.item)
                    if (aimingAtTile(rotated, target.target)) {
                        maintain(FaceRequest(dir))
                        return@forEach
                    }
                }
            }
        }
    }

    // If the player is standing on top of a item it is trying to collect, keep moving in place
    // until the item is collected. Minicraft only allows items to be picked up while moving.
    rule {
        find<CurrentAction> { action == CHOP_TREES || action is MineRock || action == DIG_SAND }

        val player by find<Memory> { entity == Entity.PLAYER }
        find<MoveTarget> { target.entity == Entity.ITEM && player.intersects(target) }

        fire {
            maintain(JiggleRequest())
        }
    }

    faceClear({
        find<CurrentAction> { action is Place }
    }, { obstacle, player ->
        // Ensure the adjacent tile to the player is free.
        obstacle.entity.solid && obstacle.pos == ((player.pos) / 16) * 16 + Vector(8,8) + player.facing.vector * 16
    }, {
        maintain(KeyRequest(Key.ATTACK))
    })

    rule {
        val action by find<CurrentAction> { action is Open }
        not<MenuOpen>()
        val player by find<Memory> { entity == Entity.PLAYER }
        val aims by all<Memory>(AreaFilter { aimBox(player) })

        fire {
            if (aims.any { it.entity == (action.action as Open).entity }) {
                maintain(GuardedKeyRequest(Key.MENU))
            }
        }
    }

    // If trying to pick up a workbench while facing one with the power glove, press the Attack key.
    rule {
        find<CurrentAction> { action == PICK_UP_WORKBENCH }
        find<HeldItem> { item == Item.POWER_GLOVE }
        not<MenuOpen>()
        val player by find<Memory> { entity == Entity.PLAYER }
        val aims by all<Memory>(AreaFilter { aimBox(player) })

        fire {
            if (aims.any { it.entity == Entity.WORKBENCH }) {
                maintain(KeyRequest(Key.ATTACK))
            }
        }
    }
}

fun aimingAt(actor: Memory, target: Memory, padding: Int = 0): Boolean =
        aimBox(actor).intersects(Box(target.pos, target.pos, target.entity.r + Vector(padding, padding)))

fun aimingAtTile(actor: Memory, target: Memory): Boolean {
    val tile = ((actor.pos - Vector(0,2)) + actor.facing.vector * 12) / 16
    return target.pos / 16 == tile
}

fun aimBox(actor: Memory): Box {
    // Bounding box that target must lie in if the actor is facing down.
    val minX = -8
    val maxX = 8
    val minY = 4
    val maxY = 12

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

    return Box(Vector(minX2, minY2 - 2) + actor.pos, Vector(maxX2, maxY2 - 2) + actor.pos)
}