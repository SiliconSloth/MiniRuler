package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.EqualityFilter
import kotlin.math.*

fun RuleEngine.navigationRules() {
    rule {
        not<MoveTarget>()
        val player by find<EntityMemory> { entity == Entity.PLAYER }
        val trees by all<TileMemory> { tile == Tile.TREE }

        fire {
            trees.minBy { tree ->
                val xDiff = tree.x + 8 - (player.x + 4)
                val yDiff = tree.y + 8 - (player.y + 3)

                sqrt((xDiff*xDiff + yDiff*yDiff).toFloat())
            }?.let{
                insert(MoveTarget(it))
            }
        }
    }

    rule {
        val target by find<MoveTarget>()
        not(EqualityFilter { target.tile })

        fire {
            delete(target)
        }
    }

    rule {
        val upPress = KeyPress(Key.UP)
        val downPress = KeyPress(Key.DOWN)
        val leftPress = KeyPress(Key.LEFT)
        val rightPress = KeyPress(Key.RIGHT)

        val target by find<MoveTarget>()
        val player by find<EntityMemory> { entity == Entity.PLAYER }

        fire {
            atomic {
                val tx = target.tile.x + 8
                val ty = target.tile.y + 8
                val px = player.x
                val py = player.y

                if (tx > px + 1) {
                    replace(leftPress, rightPress)
                } else if (tx < px - 1) {
                    replace(rightPress, leftPress)
                } else {
                    delete(leftPress)
                    delete(rightPress)
                }

                if (ty > py + 1) {
                    replace(upPress, downPress)
                } else if (ty < py - 1) {
                    replace(downPress, upPress)
                } else {
                    delete(upPress)
                    delete(downPress)
                }
            }
        }
    }
}