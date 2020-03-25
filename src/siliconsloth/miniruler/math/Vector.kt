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

    operator fun times(m: Float) =
            Vector((x * m).toInt(), (y * m).toInt())

    operator fun div(d: Int) =
            Vector(x / d, y / d)

    fun dot(other: Vector): Int =
            x*other.x + y*other.y

    fun lengthSquared(): Int =
            x*x + y*y

    fun length(): Float =
            sqrt(lengthSquared().toFloat())

    fun distanceSquared(other: Vector): Int =
            (this - other).lengthSquared()

    fun distance(other: Vector): Float =
            (this - other).length()

    fun distanceSquaredFromLine(p1: Vector, p2: Vector): Int {
        val l2 = p1.distanceSquared(p2)
        if (l2 == 0) {
            return distanceSquared(p1)
        }
        val t = ((this - p1).dot(p2 - p1).toFloat() / l2).coerceIn(0f, 1f)
        val projection = p1 + (p2 - p1) * t
        return distanceSquared(projection)
    }
}