package siliconsloth.miniruler.math

import kotlin.math.max
import kotlin.math.min

class Box(corner1: Vector, corner2: Vector, padding: Int = 0) {
    val min = Vector(min(corner1.x, corner2.x) - padding, min(corner1.y, corner2.y) - padding)
    val max = Vector(max(corner1.x, corner2.x) + padding, max(corner1.y, corner2.y) + padding)

    operator fun contains(v: Vector) =
            v.x in min.x..max.x && v.y in min.y..max.y
}