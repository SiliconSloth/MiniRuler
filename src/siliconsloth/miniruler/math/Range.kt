package siliconsloth.miniruler.math

import kotlin.math.max
import kotlin.math.min

class Range(value1: Float, value2: Float) {
    val min = min(value1, value2)
    val max = max(value1, value2)

    fun intersect(other: Range): Range? {
        val newMin = max(min, other.min)
        val newMax = min(max, other.max)

        return if (newMax >= newMin) {
            Range(newMin, newMax)
        } else {
            null
        }
    }
}