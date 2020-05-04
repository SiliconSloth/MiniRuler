package siliconsloth.miniruler.math

import kotlin.math.max
import kotlin.math.min

class Box(corner1: Vector, corner2: Vector, padding: Vector = Vector(0,0)) {
    val min = Vector(min(corner1.x, corner2.x), min(corner1.y, corner2.y)) - padding
    val max = Vector(max(corner1.x, corner2.x), max(corner1.y, corner2.y)) + padding

    constructor(corner1: Vector, corner2: Vector, padding: Int) : this(corner1, corner2, Vector(padding, padding))

    operator fun contains(v: Vector) =
            v.x in min.x..max.x && v.y in min.y..max.y

    fun intersectsSegment(s1: Vector, s2: Vector): Boolean {
        // A box is enclosed by four lines parallel to the axes.
        // By finding the t parameters at which the line of the segment intersects with these four lines,
        // we can find the ranges of t for which the line intersects the x and y ranges of the box.
        // The intersection of these two t ranges gives the t range over which the line intersects the box along
        // x and y at the same time, and is therefore intersecting the box. Clipping this t range to (0,1)
        // gives the range over which the segment itself intersects the box. This function returns whether or not
        // such an intersection exists.

        val sv = s2 - s1
        val d1 = min - s1
        val d2 = max - s1

        val tx1 = d1.x.toFloat() / sv.x
        val tx2 = d2.x.toFloat() / sv.x
        val ty1 = d1.y.toFloat() / sv.y
        val ty2 = d2.y.toFloat() / sv.y

        return Range(tx1, tx2).intersect(Range(ty1, ty2))?.intersect(Range(0f, 1f)) != null
    }

    // Intersects if non-zero intersection along both axes.
    fun intersects(other: Box): Boolean = (
                Range(min.x.toFloat(), max.x.toFloat()).intersect(Range(other.min.x.toFloat(), other.max.x.toFloat()))
                        ?.let { it.min < it.max } == true
            ) && (
                Range(min.y.toFloat(), max.y.toFloat()).intersect(Range(other.min.y.toFloat(), other.max.y.toFloat()))
                    ?.let { it.min < it.max } == true
            )

    override fun toString(): String =
            "Box($min, $max)"
}