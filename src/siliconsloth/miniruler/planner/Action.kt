package siliconsloth.miniruler.planner

import siliconsloth.miniruler.ResourceTarget

open class Action(val name: String, val prerequisite: State, val varOps: Map<Variable<*>, Operation<*>>,
                  val cost: (State, State) -> Int = { _,_ -> 1 },
                  val resourceTarget: (State, State) -> List<ResourceTarget> = { _,_ -> listOf() }) {

    val variables = prerequisite.variables
    val operations = variables.map { varOps[it] }.toTypedArray()

    operator fun <T> get(variable: Variable<T>): Operation<T>? =
            @Suppress("UNCHECKED_CAST")
            (operations[variables.indexOf(variable)] as Operation<T>?)

    fun apply(before: State): State =
            apply(before.intersect(prerequisite)) { op, dom -> op.apply(dom) }

    fun unapply(after: State): State =
            apply(after) { op, dom -> op.unapply(dom) }.intersect(prerequisite)

    private fun apply(state: State, applyFunc: (Operation<Any>, Domain<Any>) -> Domain<*>): State =
            @Suppress("UNCHECKED_CAST")
            State(variables, operations.mapIndexed { index, operation ->
                operation?.let { op -> applyFunc(op as Operation<Any>, state.domains[index] as Domain<Any>) }
                        ?: state.domains[index]
            }.toTypedArray())

    override fun toString(): String {
        return name
    }
}