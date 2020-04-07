package siliconsloth.miniruler.planner

data class State(val variables: Map<Variable<*>, Domain<*>>): VariableContainer<Domain<*>> {
    override val varValues: Map<Variable<*>, Domain<*>>
        get() = variables

    override fun defaultValue(variable: Variable<*>): Domain<*> =
            variable.initializeDomain()

    fun intersect(other: State): State =
            @Suppress("UNCHECKED_CAST")
            State(this.zip(other).map { it.variable to
                    (it.value1 as Domain<Any>).intersect(it.value2 as Domain<out Any>) }.toMap())

    fun isValid(): Boolean =
            !variables.containsValue(NoValue())

    fun supersetOf(other: State): Boolean =
            @Suppress("UNCHECKED_CAST")
            this.zip(other).all { (it.value1 as Domain<Any>).supersetOf(it.value2 as Domain<out Any>) }

    override fun equals(other: Any?): Boolean =
            other is State && this.zip(other).all { it.value1 == it.value2 }

    override fun hashCode(): Int = 0
}