package siliconsloth.miniruler.planner

/**
 * A variable in the planning problem of type T.
 *
 * @param initializeDomain returns the full domain of the variable (e.g. all positive integers)
 */
class Variable<T>(val name: String, val initializeDomain: () -> Domain<T> = { AnyValue() }): Comparable<Variable<*>> {
    override fun toString(): String =
            name

    override fun compareTo(other: Variable<*>): Int =
            name.compareTo(other.name)

    override fun equals(other: Any?): Boolean =
            other is Variable<*> && other.name == name

    override fun hashCode(): Int = name.hashCode()
}