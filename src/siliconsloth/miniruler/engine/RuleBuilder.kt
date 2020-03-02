package siliconsloth.miniruler.engine

import siliconsloth.miniruler.engine.matches.CompleteMatch

class RuleBuilder(val engine: RuleEngine) {
    val bindings = mutableListOf<Binding<*>>()
    var fire: (CompleteMatch.() -> Unit)? = null
    var end: (CompleteMatch.() -> Unit)? = null

    inline fun <reified T: Any> find(filter: Filter<T>): Binding<T> =
            Binding(T::class, filter).also { bindings.add(it) }

    inline fun <reified T: Any> not(filter: Filter<T>) {
        bindings.add(Binding(T::class, filter, inverted = true))
    }

    inline fun <reified T: Any> find(noinline condition: T.() -> Boolean = {true}): Binding<T> =
            find(Filter(condition))

    inline fun <reified T: Any> not(noinline condition: T.() -> Boolean = {true}) =
            not(Filter(condition))

    fun fire(body: CompleteMatch.() -> Unit) {
        this.fire = body
    }

    fun end(body: CompleteMatch.() -> Unit) {
        this.end = body
    }

    fun build(): Rule =
            Rule(bindings, fire, end, engine)
}