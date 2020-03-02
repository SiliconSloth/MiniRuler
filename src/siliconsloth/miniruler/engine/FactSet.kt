package siliconsloth.miniruler.engine

class FactSet<T: Any>: FactStore<T> {
    val facts = mutableSetOf<T>()

    override fun insert(fact: T) {
        facts.add(fact)
    }

    override fun delete(fact: T) {
        facts.remove(fact)
    }

    override fun retrieveMatching(filter: Filter<T>): Iterable<T> =
            facts.filter(filter.predicate)
}