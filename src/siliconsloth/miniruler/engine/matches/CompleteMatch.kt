package siliconsloth.miniruler.engine.matches

import siliconsloth.miniruler.engine.FactUpdater
import siliconsloth.miniruler.engine.Rule
import siliconsloth.miniruler.engine.MatchAtomicBuilder
import siliconsloth.miniruler.engine.RuleEngine
import kotlin.reflect.KClass

class CompleteMatch(rule: Rule): MatchNode(rule), FactUpdater<Any> {
    val maintaining = mutableListOf<Any>()

    init {
        rule.fire?.invoke(this)
    }

    override fun applyUpdates(updates: Map<KClass<*>, List<RuleEngine.Update<*>>>) {
    }

    override fun drop()  = atomic {
        rule.end?.invoke(this@CompleteMatch)

        maintaining.forEach { fact ->
            rule.engine.maintainers[fact]?.let {
                it.remove(match)
                if (it.isEmpty()) {
                    delete(fact)
                }
            }
        }
    }

    fun atomic(updates: MatchAtomicBuilder.() -> Unit) {
        rule.engine.applyUpdates(MatchAtomicBuilder(this).apply(updates).updates)
    }

    override fun insert(fact: Any) = atomic {
        insert(fact)
    }

    override fun delete(fact: Any) = atomic {
        delete(fact)
    }

    override fun replace(old: Any, new: Any) = atomic {
        replace(old, new)
    }

    fun maintain(fact: Any) = atomic {
        maintain(fact)
    }
}