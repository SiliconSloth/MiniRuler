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
        val items by all<EntityMemory> { entity == Entity.ITEM }

        fire {
            trees.union<Spatial>(items).minBy {
                val xDiff = it.x - player.x
                val yDiff = it.y - player.y

                sqrt((xDiff*xDiff + yDiff*yDiff).toFloat())
            }?.let{
                insert(MoveTarget(it))
            }
        }
    }

    rule {
        val target by find<MoveTarget>() { target is TileMemory }
        not(EqualityFilter { target.target as TileMemory })

        fire {
            delete(target)
        }
    }

    rule {
        val target by find<MoveTarget>() { target is EntityMemory }
        not(EqualityFilter { target.target as EntityMemory })

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
                val tx = target.target.x
                val ty = target.target.y
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