package siliconsloth.miniruler.engine.builders

import siliconsloth.miniruler.engine.FactUpdater
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.matching.CompleteMatch
import kotlin.reflect.KClass

open class AtomicBuilder(val engine: RuleEngine, val match: CompleteMatch?): FactUpdater<Any> {
    val updates = mutableMapOf<KClass<*>, MutableList<RuleEngine.Update<*>>>()

    override fun insert(fact: Any) {
        updates.getOrPut(fact::class) { mutableListOf() }
                .add(RuleEngine.Update(fact, true, false, match))
    }

    override fun delete(fact: Any) {
        updates.getOrPut(fact::class) { mutableListOf() }
                .add(RuleEngine.Update(fact, false, false, match))
    }

    override fun replace(old: Any, new: Any) {
        delete(old)
        insert(new)
    }

    inline fun <reified T: Any> deleteAll() {
        engine.stores[T::class]?.allFacts()?.map { RuleEngine.Update(it, false, false, match) }?.let {
            updates.getOrPut(T::class) { mutableListOf() }.addAll(it)
        }
    }
}