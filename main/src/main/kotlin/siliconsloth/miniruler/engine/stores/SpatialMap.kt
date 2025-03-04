package siliconsloth.miniruler.engine.stores

import siliconsloth.miniruler.Spatial
import siliconsloth.miniruler.engine.filters.AllFilter
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.filters.Filter
import siliconsloth.miniruler.math.Vector
import kotlin.math.ceil
import kotlin.math.floor

class SpatialMap<T: Spatial>(val blockSize: Int = 64): FactStore<T> {
    val spatials = mutableMapOf<Vector, MutableSet<T>>()

    override fun insert(fact: T) {
        spatials.getOrPut(fact.pos / blockSize) { mutableSetOf() }
                .add(fact)
    }

    override fun delete(fact: T) {
        spatials[fact.pos / blockSize]?.remove(fact)
    }

    override fun retrieveMatching(filter: Filter<T>): Iterable<T> =
            when (filter) {
                is AllFilter -> allFacts()
                is AreaFilter -> {
                    val box = filter.box()

                    val minX = floor(box.min.x.toFloat() / blockSize).toInt()
                    val maxX = ceil(box.max.x.toFloat() / blockSize).toInt()
                    val minY = floor(box.min.y.toFloat() / blockSize).toInt()
                    val maxY = ceil(box.max.y.toFloat() / blockSize).toInt()

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