package siliconsloth.miniruler.planner

import java.lang.UnsupportedOperationException

/**
 * A unary operation that can be applied to a value of type T.
 * Could be a to-many mapping, e.g. AddArbitrary.
 */
interface Operation<T> {
    /**
     * Apply the operation to every element of the input set,
     * and return the union of the sets of possible output values for each input element.
     *
     * @param before the set of input values
     */
    fun apply(before: Domain<out T>): Domain<out T>

    /**
     * Apply the inverse of the operation to every element in the given output set,
     * and return the union of the sets of possible input values for each output element.
     *
     * @param after the set of output values
     */
    fun unapply(after: Domain<out T>): Domain<out T>
}

/**
 * Add a fixed (signed) integer to the operand.
 *
 * @param value the value to add to all operands
 */
data class Add(val value: Int): Operation<Int> {
    override fun apply(before: Domain<out Int>): Domain<out Int> =
            when (before) {
                is AnyValue -> AnyValue()
                is NoValue -> NoValue()
                is LowerBounded -> LowerBounded(before.min + value)
                is SingleValue -> SingleValue(before.value + value)
                else -> throw UnsupportedOperationException("Unknown domain type")
            }

    override fun unapply(after: Domain<out Int>): Domain<out Int> =
            when (after) {
                is AnyValue -> AnyValue()
                is NoValue -> NoValue()
                is LowerBounded -> LowerBounded(after.min - value)
                is SingleValue -> SingleValue(after.value - value)
                else -> throw UnsupportedOperationException("Unknown domain type")
            }
}

/**
 * Add any positive integer to the operand.
 * Applying this operation yields all values greater than or equal to the operand;
 * the inverse yields all values less than or equal to it.
 */
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
                is SingleValue -> AnyValue() // Should really have an upper bound but we have no UpperBounded class
                else -> throw UnsupportedOperationException("Unknown domain type")
            }
}

/**
 * Set the variable's value to a fixed value of the same type.
 */
data class Set<T>(val value: T): Operation<T> {
    override fun apply(before: Domain<out T>): Domain<out T> =
            if (before is NoValue) NoValue() else SingleValue(value)

    override fun unapply(after: Domain<out T>): Domain<out T> =
            // The result of the inverse operation will be the empty set for every value not equal to the set value,
            // or the set of all values for the set value.
            // Therefore if the set value is in after the union of these sets is AnyValue, or NoValue otherwise.
            @Suppress("UNCHECKED_CAST")
            if ((after as Domain<Any>).intersect(SingleValue(value) as Domain<Any>) is NoValue) {
                NoValue()
            } else {
                AnyValue()
            }
}