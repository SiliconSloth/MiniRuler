package siliconsloth.miniruler.engine.stores

import siliconsloth.miniruler.engine.filters.EqualityFilter
import siliconsloth.miniruler.engine.filters.Filter

class FactSet<T: Any>: FactStore<T> {
    val facts = mutableSetOf<T>()

    override fun insert(fact: T) {
        facts.add(fact)
    }

    override fun delete(fact: T) {
        facts.remove(fact)
    }

    override fun retrieveMatching(filter: Filter<T>): Iterable<T> =
        if (filter is EqualityFilter) {
            filter.target().let {
                if (facts.contains(it)) {
                    listOf(it)
                } else {
                    listOf()
                }
            }
        } else {
            facts.filter(filter.predicate)
        }
}