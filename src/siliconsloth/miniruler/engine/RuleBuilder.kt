package siliconsloth.miniruler.engine

import siliconsloth.miniruler.engine.matches.CompleteMatch

class RuleBuilder(val engine: RuleEngine) {
    val bindings = mutableListOf<Binding<*>>()
    var fire: (CompleteMatch.() -> Unit)? = null
    var end: (CompleteMatch.() -> Unit)? = null

    inline fun <reified T: Any> find(noinline condition: T.() -> Boolean = {true}): Binding<T> =
            Binding(T::class, condition).also { bindings.add(it) }

    inline fun <reified T: Any> not(noinline condition: T.() -> Boolean = {true}) {
        bindings.add(Binding(T::class, condition, inverted = true))
    }

    fun fire(body: CompleteMatch.() -> Unit) {
        this.fire = body
    }

    fun end(body: CompleteMatch.() -> Unit) {
        this.end = body
    }

    fun build(): Rule =
            Rule(bindings, fire, end, engine)
}