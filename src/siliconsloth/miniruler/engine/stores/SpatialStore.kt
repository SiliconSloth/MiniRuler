package siliconsloth.miniruler.engine.stores

import siliconsloth.miniruler.Spatial
import siliconsloth.miniruler.engine.filters.AllFilter
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.filters.Filter
import siliconsloth.miniruler.math.Vector
import kotlin.math.ceil
import kotlin.math.floor

class SpatialStore<T: Spatial>(): FactStore<T> {
    val BLOCK_SIZE = 64

    val spatials = mutableMapOf<Vector, MutableSet<T>>()

    override fun insert(fact: T) {
        spatials.getOrPut(fact.pos / BLOCK_SIZE) { mutableSetOf() }
                .add(fact)
    }

    override fun delete(fact: T) {
        spatials[fact.pos / BLOCK_SIZE]?.remove(fact)
    }

    override fun retrieveMatching(filter: Filter<T>): Iterable<T> =
            when (filter) {
                is AllFilter -> spatials.values.flatten()
                is AreaFilter -> {
                    val box = filter.box()

                    val minX = floor(box.min.x.toFloat() / BLOCK_SIZE).toInt()
                    val maxX = ceil(box.max.x.toFloat() / BLOCK_SIZE).toInt()
                    val minY = floor(box.min.y.toFloat() / BLOCK_SIZE).toInt()
                    val maxY = ceil(box.max.y.toFloat() / BLOCK_SIZE).toInt()

                    (minX..maxX).map { x ->
                        (minY..maxY).map { y ->
                            spatials.getOrDefault(Vector(x, y), mutableSetOf()).filter(filter.predicate)
                        }.flatten()
                    }.flatten()
                }
                else -> spatials.values.flatten().filter(filter.predicate)
            }

    override fun allFacts(): Iterable<T> = spatials.values.flatten()
}