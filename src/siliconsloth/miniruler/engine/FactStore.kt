package siliconsloth.miniruler.engine

interface FactStore<T: Any>: FactUpdater<T> {
    fun retrieveMatching(filter: Filter<T>): Iterable<T>
}