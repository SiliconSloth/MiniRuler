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

fun goalCost(goal: Memory): Int =
        when (goal.entity) {
            Entity.TREE -> 512
            Entity.ROCK -> 512
            Entity.ITEM -> 0
            Entity.WORKBENCH -> 0
            else -> throw InvalidParameterException("Unexpected goal type")
        }

class PathFinder(val store: SpatialMap<Memory>) {
    interface Action {
        val pos: Vector
        fun costFrom(before: Vector): Int
    }
    data class Move(override val pos: Vector): Action {
        override fun costFrom(before: Vector): Int =
                before.distance(pos).toInt()
    }
    data class AcceptGoal(val goal: Memory): Action {
        override val pos: Vector
        get() = goal.pos

        override fun costFrom(before: Vector): Int =
                before.distance(pos).toInt() + goalCost(goal)
    }

    var path = listOf<Vector>()

    var goals: Map<Vector, Memory> = mapOf()
    set(value) {
        field = value
        path = listOf()
    }
    var chosenGoal: Memory? = null

    fun setGoals(gs: Iterable<Memory>) {
        val newGoals = mutableMapOf<Vector, Memory>()
        gs.forEach { g ->
            val approaches = if (g.entity.r == Vector(8,8)) {
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
                                    it != g && it.pos == tile && it.entity.solid
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

    fun nextWaypoint(current: Vector): Vector? {
        val tile = (current / 16) * 16 + Vector(8,8)

        if (goals.isEmpty()) {
            return null
        }

        if (tile !in path) {
            path = findPath(tile)
        }

        val nextInd = path.indexOf(tile)+1
        return if (nextInd == path.size) { tile } else { path[nextInd] }
    }

    fun tileClear(pos: Vector): Boolean =
            !store.retrieveMatching(Filter { it.pos == pos && it.entity.solid }).any()

    fun computeHeuristic(pos: Vector): Float =
        goals.map { (k, v) -> k.distance(pos) + goalCost(v) }.min()!!

    fun findPath(start: Vector): List<Vector> {
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

                    nextAtions.addAll(Direction.values().map { it.vector*16 + current }
                            .filter { tileClear(it) }.map { Move(it) })

                    nextAtions.forEach { next ->
                        val newDist = dists[currentAction]!! + next.costFrom(current)
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
            }
        }

        throw RuntimeException("No path could be found")
    }

    fun buildPath(goal: Action, cameFrom: Map<Action, Vector>): List<Vector> {
        val bPath = mutableListOf(goal.pos)
        var current = goal

        while (cameFrom.containsKey(current)) {
            current = Move(cameFrom[current]!!)
            bPath.add(0, current.pos)
        }

        return bPath
    }
}