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
                is LowerBounded -> LowerBounded(before.min + value)
                is Enumeration -> Enumeration(before.values.map { it + value }.toSet())
                else -> throw UnsupportedOperationException("Unknown domain type")
            }

    override fun unapply(after: Domain<out Int>): Domain<out Int> =
            when (after) {
                is AnyValue -> AnyValue()
                is LowerBounded -> LowerBounded(after.min - value)
                is Enumeration -> Enumeration(after.values.map { it - value }.toSet())
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
                is LowerBounded -> LowerBounded(before.min)
                is Enumeration -> before.values.min()?.let { LowerBounded(it) } ?: Enumeration()
                else -> throw UnsupportedOperationException("Unknown domain type")
            }

    override fun unapply(after: Domain<out Int>): Domain<out Int> =
            when (after) {
                is AnyValue -> AnyValue()
                is LowerBounded -> AnyValue()
                // Should really have an upper bound but we have no UpperBounded class
                is Enumeration -> if (after.values.isEmpty()) after else AnyValue()
                else -> throw UnsupportedOperationException("Unknown domain type")
            }
}

/**
 * Set the variable's value to a fixed value of the same type.
 */
data class SetTo<T>(val value: T): Operation<T> {
    override fun apply(before: Domain<out T>): Domain<out T> =
            if (before is Enumeration && before.values.isEmpty()) before else Enumeration(value)

    override fun unapply(after: Domain<out T>): Domain<out T> =
            // The result of the inverse operation will be the empty set for every value not equal to the set value,
            // or the set of all values for the set value.
            // Therefore if the set value is in after the union of these sets is AnyValue, or the empty set otherwise.
            @Suppress("UNCHECKED_CAST")
            if ((after as Domain<Any>).supersetOf(Enumeration(value) as Domain<Any>)) {
                AnyValue()
            } else {
                Enumeration()
            }
}