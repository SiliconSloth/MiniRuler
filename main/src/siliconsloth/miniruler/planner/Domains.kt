package siliconsloth.miniruler.planner

import java.lang.UnsupportedOperationException

/**
 * Represents the set of values that a variable of type T is allowed to take in some context.
 */
interface Domain<T> {
    fun intersect(other: Domain<out T>): Domain<out T>
    fun supersetOf(other: Domain<out T>): Boolean
}

/**
 * The set of all values of type T.
 */
class AnyValue<T>: Domain<T> {
    override fun intersect(other: Domain<out T>): Domain<out T> =
            other

    override fun supersetOf(other: Domain<out T>): Boolean =
            true

    override fun equals(other: Any?): Boolean =
            other is AnyValue<*>

    // All instances of AnyValue should share same hash code.
    override fun hashCode(): Int = 128
}

/**
 * A finite set of values that does not contain all values of type T.
 */
data class Enumeration<T>(val values: Set<T>): Domain<T> {
    constructor(vararg values: T): this(values.toSet())

    override fun intersect(other: Domain<out T>): Domain<out T> =
            when (other) {
                is AnyValue -> this
                is LowerBounded -> Enumeration(values.filter { (it as Int) >= other.min }.toSet())
                is Enumeration -> Enumeration(values.intersect(other.values).toSet())
                else -> throw UnsupportedOperationException("Unknown domain type")
            }

    override fun supersetOf(other: Domain<out T>): Boolean =
            when (other) {
                is AnyValue -> false
                is LowerBounded -> false
                is Enumeration -> values.containsAll(other.values)
                else -> throw UnsupportedOperationException("Unknown domain type")
            }
}

/**
 * The set of integers greater than or equal to min.
 */
data class LowerBounded(val min: Int): Domain<Int> {
    override fun intersect(other: Domain<out Int>): Domain<out Int> =
            when (other) {
                is AnyValue -> this
                is LowerBounded -> if (min > other.min) this else other
                is Enumeration -> Enumeration(other.values.filter { it >= min }.toSet())
                else -> throw UnsupportedOperationException("Unknown domain type")
            }

    override fun supersetOf(other: Domain<out Int>): Boolean =
            when (other) {
                is AnyValue -> false
                is LowerBounded -> min <= other.min
                is Enumeration -> other.values.all { it >= min }
                else -> throw UnsupportedOperationException("Unknown domain type")
            }
}