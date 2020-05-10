package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.pathfinder.PathFinder

fun RuleEngine.navigationRules(pathFinder: PathFinder) {
    // Target trees.
    rule {
        find<CurrentAction> { action == CHOP_TREES }
        val tree by find<Memory> { entity == Entity.TREE }

        fire {
            maintain(PossibleTarget(tree))
        }
    }

    // Target rocks.
    rule {
        find<CurrentAction> { action == MINE_ROCK }
        val rock by find<Memory> { entity == Entity.ROCK }

        fire {
            maintain(PossibleTarget(rock))
        }
    }

    // Target items that have remained in the same location for more than 40 frames, so have probably stopped moving.
    // If the item is not yet stationary, the game will not let the player pick it up.
    rule {
        find<CurrentAction> { action == CHOP_TREES || action == MINE_ROCK }
        val camera by find<CameraLocation>()    // The camera can tell us the current frame number
        val item by find<StationaryItem> { camera.frame - since > 40 }

        fire {
            maintain(PossibleTarget(item.item))
        }
    }

    // Target workbenches if trying to open one.
    rule {
        find<CurrentAction> { action == OPEN_CRAFTING }
        val bench by find<Memory> { entity == Entity.WORKBENCH }

        fire {
            maintain(PossibleTarget(bench))
        }
    }

    rule {
        val targets by all<PossibleTarget>()

        fire {
            pathFinder.setGoals(targets.map { it.target })
        }
    }

    rule {
        find<CurrentAction> { action == CHOP_TREES || action == MINE_ROCK || action == OPEN_CRAFTING }
        all<PossibleTarget>()   // Recompute path whenever targets change
        val player by find<Memory> { entity == Entity.PLAYER }

        fire {
            val waypoint = pathFinder.nextWaypoint(player.pos)
            if (waypoint != null) {
                maintain(Waypoint(waypoint))
            }
            if (pathFinder.chosenGoal != null) {
                maintain(MoveTarget(pathFinder.chosenGoal!!))
            }
        }
    }

    // Press keys to walk towards the current target.
    rule {
        val upPress = KeyPress(Key.UP)
        val downPress = KeyPress(Key.DOWN)
        val leftPress = KeyPress(Key.LEFT)
        val rightPress = KeyPress(Key.RIGHT)

        not<MenuOpen>()
        val target by find<Waypoint>()
        val player by find<Memory> { entity == Entity.PLAYER }

        fire {
            atomic {
                val t = target.pos
                val p = player.pos

                // Try to get within 1 unit of the target position along both axes.
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