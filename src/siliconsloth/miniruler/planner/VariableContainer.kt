package siliconsloth.miniruler.planner

import kotlin.NoSuchElementException

/**
 * Classes that implement this interface contain a map of Variables to values of type T.
 * Instances of such classes conceptually have a mapping for every Variable in the planning problem,
 * but may however omit some from their map for brevity. The defaultValue() method provides the assumed
 * values of such omitted Variables.
 *
 * THE MAP MUST BE SORTED IN ASCENDING NATURAL ORDER OF THE VARIABLES.
 *
 * The main feature of this interface is the zip() method, which allows iteration over all variables
 * in a pair of VariableContainers, with missing values in either container being replaced by the default value.
 */
interface VariableContainer<T> {
    data class ValuePair<A,B>(val variable: Variable<*>, val value1: A, val value2: B)

    val varValues: Map<Variable<*>, T>

    // Returns default values for Variables in the planning problem omitted from varValues.
    fun defaultValue(variable: Variable<*>): T

    /**
     * Returns a sequence of all the Variables listed in a pair of VariableContainers.
     * For each Variable, the corresponding value from each container is also given.
     * Missing values in either container are replaced by the default value for that container.
     */
    fun <O> zip(other: VariableContainer<O>): Sequence<ValuePair<T,O>> =
            object : Sequence<ValuePair<T,O>> {
                override fun iterator(): Iterator<ValuePair<T, O>> =
                        ZipIterator(this@VariableContainer, other)
            }

    class ZipIterator<A,B>(val conA: VariableContainer<A>, val conB: VariableContainer<B>): Iterator<ValuePair<A,B>> {
        val iterA = VariableContainerIterator(conA)
        val iterB = VariableContainerIterator(conB)

        override fun hasNext(): Boolean =
                iterA.peek() != null || iterB.peek() != null

        override fun next(): ValuePair<A, B> {
            if (iterA.peek() == null && iterB.peek() == null) {
                throw NoSuchElementException()
            } else if (iterA.peek() != null && (iterB.peek()?.let { iterA.peek()!!.key < it.key } != false)) {
                val a = iterA.pop()
                return ValuePair(a.key, a.value, conB.defaultValue(a.key))
            } else if (iterB.peek() != null && (iterA.peek()?.let { iterB.peek()!!.key < it.key } != false)) {
                val b = iterB.pop()
                return ValuePair(b.key, conA.defaultValue(b.key), b.value)
            } else {
                val a = iterA.pop()
                val b = iterB.pop()
                return ValuePair(a.key, a.value, b.value)
            }
        }

        class VariableContainerIterator<T>(container: VariableContainer<T>) {
            val iter = container.varValues.iterator()
            var current = if (iter.hasNext()) iter.next() else null

            fun peek(): Map.Entry<Variable<*>, T>? = current

            fun pop(): Map.Entry<Variable<*>, T> {
                val c = current!!
                current = if (iter.hasNext()) iter.next() else null
                assert(current?.let { it.key > c.key } != false) { "Keys must be in ascending natural order" }
                return c
            }
        }
    }
}
