package siliconsloth.miniruler.planner

/**
 * Classes that implement this interface contain a map of Variables to values of type T.
 * Instances of such classes conceptually have a mapping for every Variable in the planning problem,
 * but may however omit some from their map for brevity. The defaultValue() method provides the assumed
 * values of such omitted Variables.
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
        private val allVars = conA.varValues.keys.union(conB.varValues.keys).iterator()

        override fun hasNext(): Boolean =
                allVars.hasNext()

        override fun next(): ValuePair<A, B> {
            val vr = allVars.next()
            val vlA = conA.varValues.getOrDefault(vr, conA.defaultValue(vr))
            val vlB = conB.varValues.getOrDefault(vr, conB.defaultValue(vr))

            return ValuePair(vr, vlA, vlB)
        }
    }
}
