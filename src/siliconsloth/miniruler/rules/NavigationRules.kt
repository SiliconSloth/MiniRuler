package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.EqualityFilter
import kotlin.math.*

fun RuleEngine.navigationRules() {
    rule {
        not<MoveTarget>()
        val camera by find<CameraLocation>()
        val player by find<EntityMemory> { entity == Entity.PLAYER }
        val trees by all<TileMemory> { tile == Tile.TREE }
        val items by all<StationaryItem> { camera.frame - since > 20 }

        fire {
            trees.union<Spatial>(items.map { it.item }).minBy {
                it.pos.distance(player.pos)
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
                val t = target.target.pos
                val p = player.pos

                if (t.x > p.x + 1) {
                    replace(leftPress, rightPress)
                } else if (t.x < p.x - 1) {
                    replace(rightPress, leftPress)
                } else {
                    delete(leftPress)
                    delete(rightPress)
                }

                if (t.y > p.y + 1) {
                    replace(upPress, downPress)
                } else if (t.y < p.y - 1) {
                    replace(downPress, upPress)
                } else {
                    delete(upPress)
                    delete(downPress)
                }
            }
        }
    }
}