package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.filters.EqualityFilter
import siliconsloth.miniruler.math.Box

fun RuleEngine.navigationRules() {
    rule {
        val tree by find<Memory> { entity == Entity.TREE }

        fire {
            insert(PossibleTarget(tree))
        }
    }

    rule {
        val camera by find<CameraLocation>()
        val item by find<StationaryItem> { camera.frame - since > 20 }

        fire {
            insert(PossibleTarget(item.item))
        }
    }

    rule {
        not<MoveTarget>()
        val player by find<Memory> { entity == Entity.PLAYER }
        val target by find<PossibleTarget>()
        val obstacles by all<Memory>(AreaFilter { Box(player.pos, target.target.pos, padding=16) })

        fire {
            val obstructed = obstacles.any { obs ->
                if (obs == player || obs == target.target || !obs.entity.solid) {
                    false
                } else {
                    obs.pos.distanceSquaredFromLine(player.pos, target.target.pos) < 100
                }
            }
            if (!obstructed) {
                maintain(TargetProposal(target.target, target.target.pos.distance(player.pos)))
            }
        }
    }

    rule {
        not<MoveTarget>()
        val player by find<Memory> { entity == Entity.PLAYER }
        val targets by all<TargetProposal>()

        fire {
            targets.minBy {
                it.target.pos.distanceSquared(player.pos)
            }?.let{
                insert(MoveTarget(it.target))
            }
        }
    }

    rule {
        val target by find<PossibleTarget>()
        not(EqualityFilter { target.target })

        fire {
            delete(target)
        }
    }

    rule {
        val target by find<TargetProposal>()
        not(EqualityFilter { target.target })

        fire {
            delete(target)
        }
    }

    rule {
        val target by find<MoveTarget>()
        not(EqualityFilter { target.target })

        fire {
            delete(target)
        }
    }

//    rule {
//        val target by find<MoveTarget>()
//        val camera by find<CameraLocation>()
//        val player by find<Memory> { entity == Entity.PLAYER }
//        val item by find<StationaryItem> { camera.frame - since > 20
//                && item.pos.distance(player.pos) < target.target.pos.distance(player.pos) }
//
//        fire {
//            replace(target, MoveTarget(item.item))
//        }
//    }

    rule {
        val upPress = KeyPress(Key.UP)
        val downPress = KeyPress(Key.DOWN)
        val leftPress = KeyPress(Key.LEFT)
        val rightPress = KeyPress(Key.RIGHT)

        val target by find<MoveTarget>()
        val player by find<Memory> { entity == Entity.PLAYER }

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