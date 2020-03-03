package siliconsloth.miniruler

import com.mojang.ld22.Game
import siliconsloth.miniruler.engine.FactStore
import siliconsloth.miniruler.engine.Filter
import siliconsloth.miniruler.engine.RuleEngine
import kotlin.math.ceil
import kotlin.math.floor

class SpatialStore<T: Spatial>(): FactStore<T> {
    val BLOCK_SIZE = 64

    val spatials = mutableMapOf<Pair<Int, Int>, MutableSet<T>>()

    override fun insert(fact: T) {
        val coords = Pair(fact.x/BLOCK_SIZE, fact.y/BLOCK_SIZE)
        spatials.getOrPut(coords) { mutableSetOf() }
                .add(fact)
    }

    override fun delete(fact: T) {
        val coords = Pair(fact.x/BLOCK_SIZE, fact.y/BLOCK_SIZE)
        spatials[coords]?.remove(fact)
    }

    override fun retrieveMatching(filter: Filter<T>): Iterable<T> =
        if (filter is AreaFilter) {
            val minX = floor(filter.minX().toFloat() / BLOCK_SIZE).toInt()
            val maxX = ceil(filter.maxX().toFloat() / BLOCK_SIZE).toInt()
            val minY = floor(filter.minY().toFloat() / BLOCK_SIZE).toInt()
            val maxY = ceil(filter.maxY().toFloat() / BLOCK_SIZE).toInt()

            (minX..maxX).map { x ->
                (minY..maxY).map { y ->
                    spatials.getOrDefault(Pair(x, y), mutableSetOf()).filter(filter.predicate)
                }.flatten()
            }.flatten()
        } else {
            spatials.values.flatten().filter(filter.predicate)
        }
}