package siliconsloth.miniruler.planner

class Action(val name: String, val prerequisite: State, val operations: Map<Variable<*>, Operation<*>>):
        VariableContainer<Operation<*>?> {

    override val varValues: Map<Variable<*>, Operation<*>>
        get() = operations

    override fun defaultValue(variable: Variable<*>): Operation<*>? =
            null

    fun apply(before: State): State =
            apply(before.intersect(prerequisite)) { op, dom -> op.apply(dom) }

    fun unapply(after: State): State =
            apply(after) { op, dom -> op.unapply(dom) }.intersect(prerequisite)

    private fun apply(state: State, applyFunc: (Operation<Any>, Domain<Any>) -> Domain<*>): State =
            State(this.zip(state).map { vs -> vs.variable to (
                    @Suppress("UNCHECKED_CAST")
                    vs.value1?.let { applyFunc(vs.value1 as Operation<Any>, vs.value2 as Domain<Any>)
                } ?: vs.value2) }.toMap())

    override fun toString(): String {
        return name
    }
}