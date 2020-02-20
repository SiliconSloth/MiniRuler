package siliconsloth.miniruler.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlin.reflect.KClass
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import siliconsloth.miniruler.engine.matches.CompleteMatch

class RuleEngine(val scope: CoroutineScope): FactStore {
    data class Update<T: Any>(val fact: T, val isInsert: Boolean, val maintainer: CompleteMatch? = null)

    val rules = mutableMapOf<KClass<*>, MutableList<Rule>>()
    val facts = mutableMapOf<KClass<*>, MutableSet<Any>>()
    val maintainers = mutableMapOf<Any, MutableList<CompleteMatch>>()

    private val updateHandler = scope.actor<Map<KClass<*>, List<Update<*>>>>(capacity=UNLIMITED) {
        consumeEach { updates ->
            updates.values.flatten().forEach {
                if (it.isInsert) {
                    facts.getOrPut(it.fact::class) { mutableSetOf() }.add(it.fact)
                    if (it.maintainer != null) {
                        maintainers.getOrPut(it.fact) { mutableListOf() }.add(it.maintainer)
                    }
                } else {
                    facts[it.fact::class]?.remove(it.fact)
                    maintainers.remove(it.fact)
                }
            }

            updates.keys.forEach {
                rules[it]?.forEach {
                    it.updateHandler.send(updates)
                }
            }
        }
    }

    fun rule(definition: RuleBuilder.() -> Unit) {
        val rule = RuleBuilder(this).apply(definition).build()
        rule.bindings.forEach {
            rules.getOrPut(it.type) { mutableListOf() }.add(rule)
        }
    }

    fun applyUpdates(updates: Map<KClass<*>, List<Update<*>>>) = runBlocking {
        updateHandler.send(updates)
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