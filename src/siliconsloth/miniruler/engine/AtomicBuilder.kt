package siliconsloth.miniruler.engine

import kotlin.reflect.KClass

open class AtomicBuilder: FactStore {
    val updates = mutableMapOf<KClass<*>, MutableList<RuleEngine.Update<*>>>()

    override fun insert(fact: Any) {
        updates.getOrPut(fact::class) { mutableListOf() }
                .add(RuleEngine.Update(fact, true))
    }

    override fun delete(fact: Any) {
        updates.getOrPut(fact::class) { mutableListOf() }
                .add(RuleEngine.Update(fact, false))
    }

    override fun update(old: Any, new: Any) {
        delete(old)
        insert(new)
    }
}