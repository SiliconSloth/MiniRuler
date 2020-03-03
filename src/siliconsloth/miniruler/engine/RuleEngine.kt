package siliconsloth.miniruler.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlin.reflect.KClass
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import siliconsloth.miniruler.engine.builders.AtomicBuilder
import siliconsloth.miniruler.engine.builders.RuleBuilder
import siliconsloth.miniruler.engine.matching.CompleteMatch
import siliconsloth.miniruler.engine.stores.FactSet
import siliconsloth.miniruler.engine.stores.FactStore

class RuleEngine(val scope: CoroutineScope): FactUpdater<Any> {
    data class Update<T: Any>(val fact: T, val isInsert: Boolean, val maintainer: CompleteMatch? = null)

    val rules = mutableMapOf<KClass<*>, MutableList<Rule>>()
    val stores = mutableMapOf<KClass<*>, FactStore<out Any>>()
    val maintainers = mutableMapOf<Any, MutableList<CompleteMatch>>()

    private val updateHandler = scope.actor<Map<KClass<*>, List<Update<*>>>>(capacity=UNLIMITED) {
        consumeEach { updates ->
            updates.forEach {
                applyUpdates(it.key as KClass<Any>, it.value as List<Update<Any>>)
            }

            updates.keys.forEach {
                rules[it]?.forEach {
//                    it.updateHandler.send(updates)
                    it.matches.applyUpdates(updates)
                }
            }
        }
    }

    private fun <T: Any> applyUpdates(type: KClass<T>, updates: List<Update<T>>) {
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

    fun applyUpdates(updates: Map<KClass<*>, List<Update<*>>>) = runBlocking {
//        updateHandler.send(updates)
        updates.forEach {
            applyUpdates(it.key as KClass<Any>, it.value as List<Update<Any>>)
        }

        updates.keys.forEach {
            rules[it]?.forEach {
                //                    it.updateHandler.send(updates)
                it.matches.applyUpdates(updates)
            }
        }
    }

    fun atomic(updates: AtomicBuilder.() -> Unit) =
        applyUpdates(AtomicBuilder().apply(updates).updates)

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