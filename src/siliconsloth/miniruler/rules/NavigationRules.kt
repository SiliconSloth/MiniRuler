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
        find<CurrentAction> { action is MineRock }
        val rock by find<Memory> { entity == Entity.ROCK }

        fire {
            maintain(PossibleTarget(rock))
        }
    }

    rule {
        find<CurrentAction> { action == DIG_SAND }
        val sand by find<Memory> { entity == Entity.SAND }

        fire {
            maintain(PossibleTarget(sand))
        }
    }

    // Target items that have remained in the same location for more than 40 frames, so have probably stopped moving.
    // If the item is not yet stationary, the game will not let the player pick it up.
    rule {
        find<CurrentAction> { action == CHOP_TREES || action is MineRock }
        val camera by find<CameraLocation>()    // The camera can tell us the current frame number
        val item by find<StationaryItem> { camera.frame - since > 40 }

        fire {
            maintain(PossibleTarget(item.item))
        }
    }

    // Target workbenches if trying to open one.
    rule {
        find<CurrentAction> { action == OPEN_CRAFTING || action == PICK_UP_WORKBENCH }
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
        val monsters by all<Memory> { entity == Entity.SLIME || entity == Entity.ZOMBIE }

        fire {
            pathFinder.monsters = monsters.toList()
        }
    }

    rule {
        find<StaminaLevel>()

        fire {
            pathFinder.path = listOf()
        }
    }

    rule {
        all<PossibleTarget>()   // Recompute path whenever targets change
        all<Memory> { entity == Entity.SLIME || entity == Entity.ZOMBIE }
        val stamina by find<StaminaLevel>()
        val player by find<Memory> { entity == Entity.PLAYER }

        fire {
            val waypoint = pathFinder.nextWaypoint(player.pos, stamina.stamina)
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
        not<MenuOpen>()
        val target by find<Waypoint>()
        val player by find<Memory> { entity == Entity.PLAYER }

        fire {
            atomic {
                val t = target.pos
                val p = player.pos

                // Try to get within 1 unit of the target position along both axes.
                if (t.x > p.x + 1) {
                    maintain(MoveRequest(Direction.RIGHT))
                } else if (t.x < p.x - 1) {
                    maintain(MoveRequest(Direction.LEFT))
                }

                if (t.y > p.y + 1) {
                    maintain(MoveRequest(Direction.DOWN))
                } else if (t.y < p.y - 1) {
                    maintain(MoveRequest(Direction.UP))
                }
            }
        }
    }
}