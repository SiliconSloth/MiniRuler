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

    // Before trying to place the workbench release all previously held keys to prevent unwanted movement.
    rule {
        find<CurrentAction> { action == PLACE_WORKBENCH }

        fire {
            Key.values().forEach { delete(KeyPress(it)) }
        }
    }

    // When trying to place a workbench, turn to point in a direction where there is empty space in front of the player
    // then place the workbench.
    faceClear({
        find<CurrentAction> { action == PLACE_WORKBENCH }
    }, {
        it.entity.solid
    }, {
        maintain(KeyPress(Key.ATTACK))
    })

    // If trying to open the crafting menu while facing a workbench, press the Menu key.
    rule {
        find<CurrentAction> { action == OPEN_CRAFTING }
        not<MenuOpen>()
        val player by find<Memory> { entity == Entity.PLAYER }
        val aims by all<Memory>(AreaFilter { aimBox(player, 8) })

        fire {
            if (aims.any { it.entity == Entity.WORKBENCH }) {
                maintain(KeyPress(Key.MENU))
            }
        }
    }

    // If trying to pick up a workbench while facing one with the power glove, press the Attack key.
    rule {
        find<CurrentAction> { action == PICK_UP_WORKBENCH }
        find<HeldItem> { item == Item.POWER_GLOVE }
        not<MenuOpen>()
        val player by find<Memory> { entity == Entity.PLAYER }
        val aims by all<Memory>(AreaFilter { aimBox(player, 8) })

        fire {
            if (aims.any { it.entity == Entity.WORKBENCH }) {
                maintain(KeyPress(Key.ATTACK))
            }
        }
    }
}

fun aimingAt(actor: Memory, target: Spatial): Boolean =
        target.pos in aimBox(actor, 24)

fun aimBox(actor: Memory, reach: Int): Box {
    // Bounding box that target must lie in if the actor is facing down.
    val minX = -6
    val maxX = 6
    val minY = 2
    val maxY = reach

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