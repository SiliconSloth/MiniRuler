package siliconsloth.miniruler.math

class PartialOrder<T>() {
    val successors = mutableMapOf<T, MutableSet<T>>()
    val predecessors = mutableMapOf<T, MutableSet<T>>()

    constructor(original: PartialOrder<T>): this() {
        successors.putAll(original.successors.mapValues { (_,v) -> HashSet(v) })
        predecessors.putAll(original.predecessors.mapValues { (_,v) -> HashSet(v) })
    }

    fun add(lesser: T, greater: T) {
        val succs = successors.getOrPut(lesser) { mutableSetOf() }
        val preds = predecessors.getOrPut(greater) { mutableSetOf() }

        succs.add(greater)
        preds.add(lesser)

        successors[greater]?.let { succs.addAll(it) }
        predecessors[lesser]?.let { preds.addAll(it) }

        predecessors[lesser]?.forEach {
            successors[it]!!.addAll(succs)
        }
        successors[greater]?.forEach {
            predecessors[it]!!.addAll(preds)
        }
    }

    fun plus(lesser: T, greater: T): PartialOrder<T> =
            PartialOrder(this).apply { add(lesser, greater) }

    fun precedes(lesser: T, greater: T): Boolean =
        successors[lesser]?.let { greater in it } == true

    fun replace(old: T, new: T) {
        successors.remove(old)?.let {
            successors[new] = it
        }

        predecessors.remove(old)?.let {
            predecessors[new] = it
        }

        successors.values.forEach {
            if (it.remove(old)) {
                it.add(new)
            }
        }

        predecessors.values.forEach {
            if (it.remove(old)) {
                it.add(new)
            }
        }
    }
}