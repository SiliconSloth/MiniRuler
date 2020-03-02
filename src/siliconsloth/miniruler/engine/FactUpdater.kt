package siliconsloth.miniruler.engine

interface FactUpdater<T> {
    fun insert(fact: T)

    fun delete(fact: T)

    fun replace(old: T, new: T) {
        delete(old)
        insert(new)
    }
}