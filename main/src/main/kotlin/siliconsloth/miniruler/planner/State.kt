package siliconsloth.miniruler.planner

import java.util.*

/**
 * Represents a set of states. Each conceptual state in the set is a tuple of values assigned to each variable
 * in the planning problem. A State instance represents the cross product of the variable domains it specifies.
 * Variables in the problem that are not included in the State default to
 * their full domains given by Variable.initializeDomain().
 */
class State(val variables: Array<Variable<*>>, val domains: Array<Domain<*>>): Sequence<Pair<Variable<*>, Domain<*>>> {

    constructor(variables: Array<Variable<*>>, valMap: Map<Variable<*>, Domain<*>>):
            this(variables, variables.map { valMap[it] ?: it.initializeDomain() }.toTypedArray())

    operator fun <T> get(variable: Variable<T>): Domain<T> =
            @Suppress("UNCHECKED_CAST")
            (domains[variables.indexOf(variable)] as Domain<T>)

    fun intersect(other: State): State =
            @Suppress("UNCHECKED_CAST")
            State(variables, domains.mapIndexed { index, domain -> (domain as Domain<Any>)
                    .intersect(other.domains[index] as Domain<Any>) }.toTypedArray() as Array<Domain<*>>)

    // States that contain empty domains are impossible, as there is a variable that cannot be assigned a value.
    fun isValid(): Boolean =
            !domains.any { it is Enumeration && it.values.isEmpty() }

    fun supersetOf(other: State): Boolean =
            @Suppress("UNCHECKED_CAST")
            variables.indices.all { (domains[it] as Domain<Any>).supersetOf(other.domains[it] as Domain<Any>) }

    override fun equals(other: Any?): Boolean =
            other is State && domains.contentEquals(other.domains)

    // Comparing States is complicated so use a simple, inefficient hash code until something faster is needed.
    override fun hashCode(): Int = 0

    override fun toString(): String =
            "State" + variables.zip(domains).filter { it.second != LowerBounded(0) && it.second != Enumeration(0)
                    && it.second != Enumeration(false) && it.second !is AnyValue }.toMap()

    override fun iterator(): Iterator<Pair<Variable<*>, Domain<*>>> =
            variables.zip(domains).iterator()
}