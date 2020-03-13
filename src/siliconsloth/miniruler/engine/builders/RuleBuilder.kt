package siliconsloth.miniruler.engine.builders

import siliconsloth.miniruler.engine.bindings.Binding
import siliconsloth.miniruler.engine.Rule
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.bindings.InvertedBinding
import siliconsloth.miniruler.engine.bindings.SimpleBinding
import siliconsloth.miniruler.engine.filters.AllFilter
import siliconsloth.miniruler.engine.filters.Filter
import siliconsloth.miniruler.engine.matching.CompleteMatch

class RuleBuilder(val engine: RuleEngine) {
    val bindings = mutableListOf<Binding<*,*>>()
    var fire: (CompleteMatch.() -> Unit)? = null
    var end: (CompleteMatch.() -> Unit)? = null

    inline fun <reified T: Any> find(filter: Filter<T>): SimpleBinding<T> =
            SimpleBinding(T::class, filter).also { bindings.add(it) }

    inline fun <reified T: Any> not(filter: Filter<T>) {
        bindings.add(InvertedBinding(T::class, filter))
    }

    inline fun <reified T: Any> find(noinline condition: T.() -> Boolean): SimpleBinding<T> =
            find(Filter(condition))

    inline fun <reified T: Any> not(noinline condition: T.() -> Boolean) =
            not(Filter(condition))

    inline fun <reified T: Any> find(): SimpleBinding<T> =
            find(AllFilter())

    inline fun <reified T: Any> not() =
            not(AllFilter<T>())

    fun fire(body: CompleteMatch.() -> Unit) {
        this.fire = body
    }

    fun end(body: CompleteMatch.() -> Unit) {
        this.end = body
    }

    fun build(): Rule =
            Rule(bindings, fire, end, engine)
}