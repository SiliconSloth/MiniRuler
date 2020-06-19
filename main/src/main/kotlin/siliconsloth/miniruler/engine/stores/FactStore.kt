package siliconsloth.miniruler.engine.stores

import siliconsloth.miniruler.engine.FactUpdater
import siliconsloth.miniruler.engine.filters.Filter

interface FactStore<T: Any>: FactUpdater<T> {
    fun retrieveMatching(filter: Filter<T>): Iterable<T>

    fun allFacts(): Iterable<T>
}