package siliconsloth.miniruler.math

class Box(val min: Vector, val max: Vector) {
    operator fun contains(v: Vector) =
            v.x in min.x..max.x && v.y in min.y..max.y
}