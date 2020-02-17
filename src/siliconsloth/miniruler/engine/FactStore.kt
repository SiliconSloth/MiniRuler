package siliconsloth.miniruler.engine

interface FactStore {
    fun insert(fact: Any)

    fun delete(fact: Any)

    fun update(old: Any, new: Any) {
        delete(old)
        insert(new)
    }
}