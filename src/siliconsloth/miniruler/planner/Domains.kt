package siliconsloth.miniruler.planner

import java.lang.UnsupportedOperationException

interface Domain<T> {
    fun intersect(other: Domain<out T>): Domain<out T>
    fun supersetOf(other: Domain<out T>): Boolean
}

class AnyValue<T>: Domain<T> {
    override fun intersect(other: Domain<out T>): Domain<out T> =
            other

    override fun supersetOf(other: Domain<out T>): Boolean =
            true

    override fun equals(other: Any?): Boolean =
            other is AnyValue<*>

    override fun hashCode(): Int = 128
}

class NoValue: Domain<Nothing> {
    override fun intersect(other: Domain<out Nothing>): Domain<Nothing> =
            this

    override fun equals(other: Any?): Boolean =
            other is NoValue

    override fun supersetOf(other: Domain<out Nothing>): Boolean =
            other is NoValue

    override fun hashCode(): Int = 129
}

data class LowerBounded(val min: Int): Domain<Int> {
    override fun intersect(other: Domain<out Int>): Domain<out Int> =
            when (other) {
                is AnyValue -> this
                is NoValue -> other
                is LowerBounded -> if (min > other.min) this else other
                is SingleValue -> if (other.value >= min) other else NoValue()
                else -> throw UnsupportedOperationException("Unknown domain type")
            }

    override fun supersetOf(other: Domain<out Int>): Boolean =
            when (other) {
                is AnyValue -> false
                is NoValue -> true
                is LowerBounded -> min <= other.min
                is SingleValue -> other.value >= min
                else -> throw UnsupportedOperationException("Unknown domain type")
            }
}

data class SingleValue<T>(val value: T): Domain<T> {
    override fun intersect(other: Domain<out T>): Domain<out T> =
            when (other) {
                is AnyValue -> this
                is NoValue -> other
                is LowerBounded -> if (value as Int >= other.min) this else NoValue()
                is SingleValue -> if (value == other.value) this else NoValue()
                else -> throw UnsupportedOperationException("Unknown domain type")
            }

    override fun supersetOf(other: Domain<out T>): Boolean =
            when (other) {
                is AnyValue -> false
                is NoValue -> true
                is LowerBounded -> false
                is SingleValue -> value == other.value
                else -> throw UnsupportedOperationException("Unknown domain type")
            }
}