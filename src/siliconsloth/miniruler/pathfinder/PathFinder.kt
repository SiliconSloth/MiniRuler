package siliconsloth.miniruler.pathfinder

import siliconsloth.miniruler.Direction
import siliconsloth.miniruler.Entity
import siliconsloth.miniruler.Memory
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.filters.Filter
import siliconsloth.miniruler.engine.stores.SpatialMap
import siliconsloth.miniruler.math.Box
import siliconsloth.miniruler.math.Vector
import java.security.InvalidParameterException
import java.util.*
import kotlin.math.pow

fun goalCost(goal: Memory): Int =
        when (goal.entity) {
            Entity.TREE -> 512
            Entity.ROCK -> 512
            Entity.SAND -> 512
            Entity.ITEM -> 0
            Entity.WORKBENCH -> 0
            Entity.FURNACE -> 0
            else -> throw InvalidParameterException("Unexpected goal type")
        }

class PathFinder(val store: SpatialMap<Memory>) {
    interface Action {
        val pos: Vector
        fun costFrom(before: Vector, hasGoals: Boolean, dangerCost: Float): Int
    }

    data class Move(override val pos: Vector): Action {
        override fun costFrom(before: Vector, hasGoals: Boolean, dangerCost: Float): Int =
                before.distance(pos).toInt() + dangerCost.toInt()
    }

    data class AcceptGoal(val goal: Memory): Action {
        override val pos: Vector
        get() = goal.pos

        override fun costFrom(before: Vector, hasGoals: Boolean, dangerCost: Float): Int =
                before.distance(pos).toInt() + goalCost(goal) + (dangerCost * 10).toInt()
    }

    data class Terminate(override val pos: Vector, val explorable: Boolean): Action {
        override fun costFrom(before: Vector, hasGoals: Boolean, dangerCost: Float): Int =
            before.distance(pos).toInt() + (if (hasGoals) 800 else 0) +
                    (if (explorable) 0 else 800) + (dangerCost * 10).toInt()
    }

    var path = listOf<Vector>()

    var goals = mapOf<Vector, Memory>()
    set(value) {
        field = value
        path = listOf()
    }
    var chosenGoal: Memory? = null

    var monsters = listOf<Memory>()
    set(value) {
        field = value
        path = listOf()
    }

    fun setGoals(gs: Iterable<Memory>) {
        val newGoals = mutableMapOf<Vector, Memory>()
        gs.forEach { g ->
            val approaches = if (g.entity.solid || g.entity == Entity.SAND) {
                Direction.values().map { g.pos + it.vector * 16 }
            } else {
                listOf(g.pos)
            }

            approaches.forEach { app ->
                val obstacles = store.retrieveMatching(AreaFilter { Box(app, app, padding = 16) })
                val minTile = ((app - Vector(8, 8)) / 16) * 16 + 8
                for (y in 0..1) {
                    for (x in 0..1) {
                        val tile = minTile + Vector(x * 16, y * 16)
                        if (Box(app, app, padding = g.entity.r)
                                        .intersects(Box(tile, tile, padding = 8))
                                && !obstacles.any {
                                    it != g && it.pos == tile && (it.entity.solid || it.entity == Entity.WATER)
                                            && it.entity.r == Vector(8, 8)
                                }) {

                            if (newGoals[tile]?.let { goalCost(it) > goalCost(g) } != false) {
                                newGoals[tile] = g
                            }
                        }
                    }
                }
            }
        }
        goals = newGoals
    }

    fun nextWaypoint(current: Vector, stamina: Int): Vector? {
        val tile = (current / 16) * 16 + Vector(8,8)

        if (!(tile in path && pathClear())) {
            path = findPath(tile, stamina)
        }

        if (path.isEmpty()) {
            return null
        }

        val nextInd = path.lastIndexOf(tile)+1
        if (nextInd < path.size) {
            return path[nextInd]
        } else if (goals.isEmpty()) {
            path = findPath(tile, stamina)
            return  if (path.isEmpty()) null else path[1]
        } else {
            return tile
        }
    }

    fun tileClear(pos: Vector): Boolean =
            !store.retrieveMatching(Filter { it.pos == pos && it.entity.solid }).any()

    fun tileExplorable(pos: Vector): Boolean {
        val tiles = store.retrieveMatching(AreaFilter { Box(pos, pos, padding = 40) })
        return (!tiles.any { it.pos == pos && it.entity.r == Vector(8,8) })
                && tiles.count { it.entity != Entity.WATER } > 4
    }

    fun tileWater(pos: Vector): Boolean =
            store.retrieveMatching(Filter { it.pos == pos && it.entity == Entity.WATER }).any()

    fun pathClear(): Boolean =
            path.all { tileClear(it) }

    fun computeHeuristic(pos: Vector): Float =
        goals.map { (k, v) -> k.distance(pos) + goalCost(v) }.min() ?: 0f

    fun monsterCost(pos: Vector): Float {
        val dist = monsters.map { it.pos.distance(pos) }.min()
        if (dist?.let { it > 400 } != false) {
            return 0f
        } else {
            return 1600 * 0.9f.pow(dist)
        }
    }

    fun findPath(start: Vector, stamina: Int): List<Vector> {
        val frontier = PriorityQueue<Pair<Action, Float>>(compareBy { it.second })
        frontier.add(Pair(Move(start), computeHeuristic(start)))

        val cameFrom = mutableMapOf<Action, Vector>()
        val dists = mutableMapOf<Action, Int>()
        dists[Move(start)] = 0

        while (frontier.any()) {
            val currentAction = frontier.poll().first
            when (currentAction) {
                is Move -> {
                    val current = currentAction.pos
                    val nextAtions = mutableListOf<Action>()

                    if (current in goals) {
                        nextAtions.add(AcceptGoal(goals[current]!!))
                    }

                    nextAtions.add(Terminate(current, tileExplorable(current)))

                    nextAtions.addAll(Direction.values().map { it.vector*16 + current }
                            .filter { tileClear(it) }.map { Move(it) })

                    nextAtions.forEach { next ->
                        var dangerCost = monsterCost(next.pos)
                        if (tileWater(next.pos)) {
                            dangerCost += (11 - stamina) * (if (next is Move) 16 else 1600)
                        }

                        val newDist = dists[currentAction]!! + next.costFrom(current, goals.isNotEmpty(), dangerCost)
                        if (dists[next]?.let { newDist < it } != false) {
                            cameFrom[next] = current
                            dists[next] = newDist

                            val nextHeuristic = if (next is Move) {
                                computeHeuristic(next.pos)
                            } else {
                                0f
                            }

                            frontier.removeIf { it.first == next }
                            frontier.add(Pair(next, newDist + nextHeuristic))
                        }
                    }
                }

                is AcceptGoal -> {
                    chosenGoal = currentAction.goal
                    return buildPath(currentAction, cameFrom)
                }

                is Terminate -> {
                    chosenGoal = null
                    return buildPath(currentAction, cameFrom)
                }
            }
        }

        return listOf()
    }

    fun buildPath(goal: Action, cameFrom: Map<Action, Vector>): List<Vector> {
        val bPath = mutableListOf(goal.pos)
        var current = goal

        while (cameFrom.containsKey(current)) {
            current = Move(cameFrom[current]!!)
            bPath.add(0, current.pos)
        }

        // If the goal is sand, move the final waypoint to be at the edge of the target sand tile.
        if (goal is AcceptGoal && goal.goal.entity == Entity.SAND) {
            bPath[bPath.size-1] = (bPath[bPath.size-1] + bPath[bPath.size-2]) / 2
        }

        return bPath
    }
}