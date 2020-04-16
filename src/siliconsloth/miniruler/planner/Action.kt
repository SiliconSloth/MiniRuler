package siliconsloth.miniruler.planner

open class Action(val name: String, val prerequisite: State, val operations: Map<Variable<*>, Operation<*>>):
        VariableContainer<Operation<*>?> {

    // For VariableContainer
    override val varValues: Map<Variable<*>, Operation<*>>
        get() = operations

    // For VariableContainer; defaults to no operation
    override fun defaultValue(variable: Variable<*>): Operation<*>? =
            null

    fun apply(before: State): State =
            apply(before.intersect(prerequisite)) { op, dom -> op.apply(dom) }

    fun unapply(after: State): State =
            apply(after) { op, dom -> op.unapply(dom) }.intersect(prerequisite)

    private fun apply(state: State, applyFunc: (Operation<Any>, Domain<Any>) -> Domain<*>): State =
            State(this.zip(state).map { vs -> vs.variable to (
                    @Suppress("UNCHECKED_CAST") // value1: operation value2: domain
                    vs.value1?.let { applyFunc(vs.value1 as Operation<Any>, vs.value2 as Domain<Any>) } ?: vs.value2
                ) }.toMap())

    override fun toString(): String {
        return name
    }
}