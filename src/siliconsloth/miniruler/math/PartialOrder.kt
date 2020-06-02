package siliconsloth.miniruler.math

class PartialOrder<T>() {
    val successors = mutableMapOf<T, MutableSet<T>>()

    constructor(original: PartialOrder<T>): this() {
        successors.putAll(original.successors.mapValues { (_,v) -> HashSet(v) })
    }

    fun add(lesser: T, greater: T) {
        val succs = successors.getOrPut(lesser) { mutableSetOf() }
        succs.add(greater)
        successors[greater]?.let { succs.addAll(it) }
    }

    fun plus(lesser: T, greater: T): PartialOrder<T> =
            PartialOrder(this).apply { add(lesser, greater) }

    fun precedes(lesser: T, greater: T): Boolean =
        successors[lesser]?.let { greater in it } == true
}