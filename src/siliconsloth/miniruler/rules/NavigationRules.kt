package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.filters.EqualityFilter
import siliconsloth.miniruler.math.Box

fun RuleEngine.navigationRules() {
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

    // Create TargetProposals for all possible targets that are in line-of-sight of the player,
    // with no solid obstacles in the way.
    rule {
        val player by find<Memory> { entity == Entity.PLAYER }
        val target by find<PossibleTarget>()
        // All entities in the area between the player and target
        val obstacles by all<Memory>(AreaFilter { Box(player.pos, target.target.pos, padding=16) })

        fire {
            val obstructed = obstacles.any { obs ->
                obs != player && obs != target.target && obs.entity.solid &&
                    Box(obs.pos, obs.pos, padding = 8).intersectsSegment(player.pos, target.target.pos)
            }
            if (!obstructed) {
                maintain(TargetProposal(target.target))
            }
        }
    }

    // If the agent doesn't currently have a target to move towards,
    // choose the nearest TargetProposal as the new target.
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

    // Prioritise targeting items over anything else.
    rule {
        val oldTarget by find<MoveTarget> { target.entity != Entity.ITEM }
        val itemTarget by find<TargetProposal> { target.entity == Entity.ITEM }

        fire {
            replace(oldTarget, MoveTarget(itemTarget.target))
        }
    }

    // If an entity disappears all corresponding target facts should be deleted.
    rule {
        val target by find<MoveTarget>()
        not(EqualityFilter { target.target })

        fire {
            println(target.target)
            delete(target)
        }
    }

    // Stop targeting trees, rocks and items if no longer gathering wood.
    rule {
        not<CurrentAction> { action == CHOP_TREES || action == MINE_ROCK }
        val target by find<MoveTarget> { target.entity == Entity.TREE || target.entity == Entity.ROCK
                                        || target.entity == Entity.ITEM }

        fire {
            delete(target)
        }
    }

    // Press keys to walk towards the current target.
    rule {
        val upPress = KeyPress(Key.UP)
        val downPress = KeyPress(Key.DOWN)
        val leftPress = KeyPress(Key.LEFT)
        val rightPress = KeyPress(Key.RIGHT)

        not<MenuOpen>()
        val target by find<MoveTarget>()
        val player by find<Memory> { entity == Entity.PLAYER }

        fire {
            atomic {
                val t = target.target.pos
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