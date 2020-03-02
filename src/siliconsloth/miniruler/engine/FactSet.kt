package siliconsloth.miniruler.engine

class FactSet<T: Any>: FactStore<T> {
    val facts = mutableSetOf<T>()

    override fun insert(fact: T) {
        facts.add(fact)
    }

    override fun delete(fact: T) {
        facts.remove(fact)
    }

    override fun retrieveMatching(condition: (T) -> Boolean): Iterable<T> =
            facts.filter(condition)
}