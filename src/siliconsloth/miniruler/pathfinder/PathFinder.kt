package siliconsloth.miniruler.pathfinder

import siliconsloth.miniruler.Direction
import siliconsloth.miniruler.Memory
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.filters.Filter
import siliconsloth.miniruler.engine.stores.SpatialMap
import siliconsloth.miniruler.math.Box
import siliconsloth.miniruler.math.Vector
import java.util.*

class PathFinder(val store: SpatialMap<Memory>) {
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
            val obstacles = store.retrieveMatching(AreaFilter { Box(g.pos, g.pos, padding = 16) })
            val minTile = ((g.pos - Vector(8, 8)) / 16) * 16 + 8
            for (y in 0..1) {
                for (x in 0..1) {
                    val tile = minTile + Vector(x * 16, y * 16)
                    if (Box(g.pos, g.pos, padding = g.entity.r)
                                    .intersects(Box(tile, tile, padding = 8))
                            && !obstacles.any {
                                it != g && it.pos == tile && it.entity.solid
                                        && it.entity.r == Vector(8, 8)
                            }) {
                        newGoals[tile] = g
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

        if (tile in goals) {
            return goals[tile]!!.pos
        }

        if (tile !in path) {
            path = findPath(tile)
        }

        return path[path.indexOf(tile)+1]
    }

    fun tileClear(pos: Vector): Boolean =
            !store.retrieveMatching(Filter { it.pos == pos && it.entity.solid }).any()

    fun computeHeuristic(pos: Vector): Float =
        goals.keys.map { it.distance(pos) }.min()!!

    fun findPath(start: Vector): List<Vector> {
        val frontier = PriorityQueue<Pair<Vector, Float>>(compareBy { it.second })
        frontier.add(Pair(start, computeHeuristic(start)))

        val cameFrom = mutableMapOf<Vector, Vector>()
        val dists = mutableMapOf<Vector, Int>()
        dists[start] = 0

        while (frontier.any()) {
            val current = frontier.poll().first
            println(current)
            if (current in goals) {
                chosenGoal = goals[current]!!
                return buildPath(current, cameFrom)
            }

            Direction.values().map { it.vector*16 + current }.filter { tileClear(it) || it in goals }.forEach { next ->
                val newDist = dists[current]!! + current.distance(next).toInt()
                if (dists[next]?.let { newDist < it } != false) {
                    cameFrom[next] = current
                    dists[next] = newDist

                    frontier.removeIf { it.first == next }
                    frontier.add(Pair(next, newDist + computeHeuristic(next)))
                }
            }
        }

        throw RuntimeException("No path could be found")
    }

    fun buildPath(goal: Vector, cameFrom: Map<Vector, Vector>): List<Vector> {
        val bPath = mutableListOf<Vector>(goal)
        var current = goal

        while (cameFrom.containsKey(current)) {
            current = cameFrom[current]!!
            bPath.add(0, current)
        }

        return bPath
    }
}