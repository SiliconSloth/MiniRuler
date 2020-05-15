package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.filters.EqualityFilter
import siliconsloth.miniruler.math.Box
import siliconsloth.miniruler.math.Vector

fun RuleEngine.attackRules() {
    // If the player is aiming at a tree and has sufficient stamina, attack it.
    rule {
        find<CurrentAction> { action == CHOP_TREES || action is MineRock || action == DIG_SAND }

        val player by find<Memory> { entity == Entity.PLAYER }
        find<MoveTarget> { (target.entity == Entity.TREE || target.entity == Entity.ROCK || target.entity == Entity.SAND)
                && aimingAt(player, target) }
        find<StaminaLevel> { stamina > 6 }

        fire {
            maintain(KeyRequest(Key.ATTACK))
        }
    }

    // If the player is standing on top of a item it is trying to collect, keep moving in place
    // until the item is collected. Minicraft only allows items to be picked up while moving.
    rule {
        val upRequest = KeyRequest(Key.UP)
        val downRequest = KeyRequest(Key.DOWN)

        find<CurrentAction> { action == CHOP_TREES || action is MineRock }

        val player by find<Memory> { entity == Entity.PLAYER }
        find<MoveTarget> { target.entity == Entity.ITEM && player.intersects(target) }

        fire {
            if (exists(EqualityFilter { downRequest })) {
                replace(downRequest, upRequest)
            } else {
                replace(upRequest, downRequest)
            }
        }
    }

    // When trying to place a workbench, turn to point in a direction where there is empty space in front of the player
    // then place the workbench.
    faceClear({
        find<CurrentAction> { action == PLACE_WORKBENCH }
    }, { obstacle, player ->
        // Ensure the adjacent tile to the player is free.
        obstacle.entity.solid && obstacle.pos == ((player.pos) / 16) * 16 + Vector(8,8) + player.facing.vector * 16
    }, {
        maintain(KeyRequest(Key.ATTACK))
    })

    // If trying to open the crafting menu while facing a workbench, press the Menu key.
    rule {
        find<CurrentAction> { action == OPEN_CRAFTING }
        not<MenuOpen>()
        val player by find<Memory> { entity == Entity.PLAYER }
        val aims by all<Memory>(AreaFilter { aimBox(player) })

        fire {
            if (aims.any { it.entity == Entity.WORKBENCH }) {
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

fun aimingAt(actor: Memory, target: Memory): Boolean =
        aimBox(actor).intersects(Box(target.pos, target.pos, target.entity.r))

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