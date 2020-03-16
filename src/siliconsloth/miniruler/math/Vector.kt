package siliconsloth.miniruler.math

import kotlin.math.sqrt

data class Vector(val x: Int, val y: Int) {
    operator fun plus(other: Vector) =
            Vector(x + other.x, y + other.y)

    operator fun plus(p: Int) =
            Vector(x + p, y + p)

    operator fun minus(other: Vector) =
            Vector(x - other.x, y - other.y)

    operator fun times(m: Int) =
            Vector(x * m, y * m)

    operator fun div(d: Int) =
            Vector(x / d, y / d)

    fun length(): Float =
            sqrt((x*x + y*y).toFloat())

    fun distance(other: Vector): Float =
            (this - other).length()
}