package siliconsloth.miniruler.planner

import java.lang.UnsupportedOperationException

interface Operation<T> {
    fun apply(before: Domain<out T>): Domain<out T>
    fun unapply(after: Domain<out T>): Domain<out T>
}

data class Add(val operand: Int): Operation<Int> {
    override fun apply(before: Domain<out Int>): Domain<out Int> =
            when (before) {
                is AnyValue -> AnyValue()
                is NoValue -> NoValue()
                is LowerBounded -> LowerBounded(before.min + operand)
                is SingleValue -> SingleValue(before.value + operand)
                else -> throw UnsupportedOperationException("Unknown domain type")
            }

    override fun unapply(after: Domain<out Int>): Domain<out Int> =
            when (after) {
                is AnyValue -> AnyValue()
                is NoValue -> NoValue()
                is LowerBounded -> LowerBounded(after.min - operand)
                is SingleValue -> SingleValue(after.value - operand)
                else -> throw UnsupportedOperationException("Unknown domain type")
            }
}

class AddArbitrary: Operation<Int> {
    override fun apply(before: Domain<out Int>): Domain<out Int> =
            when (before) {
                is AnyValue -> AnyValue()
                is NoValue -> NoValue()
                is LowerBounded -> LowerBounded(before.min)
                is SingleValue -> LowerBounded(before.value)
                else -> throw UnsupportedOperationException("Unknown domain type")
            }

    override fun unapply(after: Domain<out Int>): Domain<out Int> =
            when (after) {
                is AnyValue -> AnyValue()
                is NoValue -> NoValue()
                is LowerBounded -> AnyValue()
                is SingleValue -> AnyValue()
                else -> throw UnsupportedOperationException("Unknown domain type")
            }
}

data class Set<T>(val value: T): Operation<T> {
    override fun apply(before: Domain<out T>): Domain<out T> =
            if (before is NoValue) NoValue() else SingleValue(value)

    override fun unapply(after: Domain<out T>): Domain<out T> =
            @Suppress("UNCHECKED_CAST")
            if ((after as Domain<Any>).intersect(SingleValue(value) as Domain<Any>) is NoValue) {
                NoValue()
            } else {
                AnyValue()
            }
}