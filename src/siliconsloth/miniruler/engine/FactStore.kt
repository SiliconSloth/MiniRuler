package siliconsloth.miniruler.engine

interface FactStore<T: Any>: FactUpdater<T> {
    fun retrieveMatching(condition: (T) -> Boolean): Iterable<T>
}