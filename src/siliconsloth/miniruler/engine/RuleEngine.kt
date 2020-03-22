package siliconsloth.miniruler.engine

import kotlin.reflect.KClass
import siliconsloth.miniruler.engine.builders.AtomicBuilder
import siliconsloth.miniruler.engine.builders.RuleBuilder
import siliconsloth.miniruler.engine.matching.CompleteMatch
import siliconsloth.miniruler.engine.stores.FactSet
import siliconsloth.miniruler.engine.stores.FactStore

class RuleEngine: FactUpdater<Any> {
    data class Update<T: Any>(val fact: T, val isInsert: Boolean, val maintainer: CompleteMatch? = null)

    val rules = mutableMapOf<KClass<*>, MutableList<Rule>>()
    val stores = mutableMapOf<KClass<*>, FactStore<out Any>>()
    val maintainers = mutableMapOf<Any, MutableList<CompleteMatch>>()

    var running = false
    val updateQueue = mutableListOf<Map<KClass<*>, List<Update<*>>>>()

    private fun <T: Any> applyUpdates(type: KClass<T>, updates: List<Update<T>>) {
        @Suppress("UNCHECKED_CAST")
        val store = stores.getOrPut(type) { FactSet<T>() } as FactStore<T>
        updates.forEach {
            if (it.isInsert) {
                store.insert(it.fact)
                if (it.maintainer != null) {
                    maintainers.getOrPut(it.fact) { mutableListOf() }.add(it.maintainer)
                }
            } else {
                store.delete(it.fact)
                maintainers.remove(it.fact)
            }
        }
    }

    inline fun <reified T: Any> addFactStore(store: FactStore<T>) {
        stores[T::class] = store
    }

    fun rule(definition: RuleBuilder.() -> Unit) {
        val rule = RuleBuilder(this).apply(definition).build()
        rule.bindings.forEach {
            rules.getOrPut(it.type) { mutableListOf() }.add(rule)
        }
    }

    fun applyUpdates(updates: Map<KClass<*>, List<Update<*>>>) {
        updateQueue.add(updates)
         if (!running) {
             running = true
             while (updateQueue.isNotEmpty()) {
                 val batch = updateQueue.removeAt(0)
                         .mapValues { it.value.filter { !(it.isInsert && it.maintainer?.dropped ?: false) } }

                 batch.forEach {
                     @Suppress("UNCHECKED_CAST")
                     applyUpdates(it.key as KClass<Any>, it.value as List<Update<Any>>)
                 }

                 val applicable = batch.keys.map { rules[it] ?: mutableListOf() }.flatten().distinct()
                 applicable.forEach {
                     it.applyUpdates(batch)
                 }
             }
             running = false
         }
    }

    fun atomic(updates: AtomicBuilder.() -> Unit) =
        applyUpdates(AtomicBuilder(this).apply(updates).updates)

    override fun insert(fact: Any) = atomic {
        insert(fact)
    }

    override fun delete(fact: Any) = atomic {
        delete(fact)
    }

    override fun replace(old: Any, new: Any) = atomic {
        replace(old, new)
    }
}