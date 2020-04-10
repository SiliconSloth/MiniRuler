package siliconsloth.miniruler.planner

/**
 * Represents a set of states. Each conceptual state in the set is a tuple of values assigned to each variable
 * in the planning problem. A State instance represents the cross product of the variable domains it specifies.
 * Variables in the problem that are not included in the State default to
 * their full domains given by Variable.initializeDomain().
 */
data class State(val variables: Map<Variable<*>, Domain<*>>): VariableContainer<Domain<*>> {
    // For VariableContainer
    override val varValues: Map<Variable<*>, Domain<*>>
        get() = variables

    // For VariableContainer; defaults to Variable's full domain
    override fun defaultValue(variable: Variable<*>): Domain<*> =
            variable.initializeDomain()

    fun intersect(other: State): State =
            @Suppress("UNCHECKED_CAST")
            State(this.zip(other).map { it.variable to
                    (it.value1 as Domain<Any>).intersect(it.value2 as Domain<out Any>) }.toMap())

    // States that contain NoValue are impossible, as there is a variable that cannot be assigned a value.
    fun isValid(): Boolean =
            !variables.containsValue(NoValue())

    fun supersetOf(other: State): Boolean =
            @Suppress("UNCHECKED_CAST")
            this.zip(other).all { (it.value1 as Domain<Any>).supersetOf(it.value2 as Domain<out Any>) }

    override fun equals(other: Any?): Boolean =
            other is State && this.zip(other).all { it.value1 == it.value2 }

    // Comparing States is complicated so use a simple, inefficient hash code until something faster is needed.
    override fun hashCode(): Int = 0
}