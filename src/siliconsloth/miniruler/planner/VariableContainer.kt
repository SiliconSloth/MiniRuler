package siliconsloth.miniruler.planner

interface VariableContainer<T> {
    data class ValuePair<A,B>(val variable: Variable<*>, val value1: A, val value2: B)

    val varValues: Map<Variable<*>, T>

    fun defaultValue(variable: Variable<*>): T

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
