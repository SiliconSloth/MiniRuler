package siliconsloth.miniruler.engine

import siliconsloth.miniruler.engine.filters.Filter

interface FactStore<T: Any>: FactUpdater<T> {
    fun retrieveMatching(filter: Filter<T>): Iterable<T>
}