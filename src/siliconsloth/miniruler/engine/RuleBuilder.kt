package siliconsloth.miniruler.engine

import kotlinx.coroutines.CoroutineScope
import siliconsloth.miniruler.engine.matches.CompleteMatch

class RuleBuilder(val engine: RuleEngine) {
    val bindings = mutableListOf<Binding<*>>()
    var body: (CompleteMatch.() -> Unit)? = null

    inline fun <reified T: Any> find(noinline condition: (T) -> Boolean): Binding<T> =
            Binding(T::class, condition).also { bindings.add(it) }

    inline fun <reified T: Any> not(noinline condition: (T) -> Boolean) {
        bindings.add(Binding(T::class, condition, inverted = true))
    }

    fun body(body: CompleteMatch.() -> Unit) {
        this.body = body
    }

    fun build(): Rule =
            Rule(bindings, body!!, engine)
}