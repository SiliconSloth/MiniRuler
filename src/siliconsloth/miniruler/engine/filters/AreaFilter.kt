package siliconsloth.miniruler.engine.filters

import siliconsloth.miniruler.Spatial

class AreaFilter<T: Spatial>(val minX: () -> Int, val maxX: () -> Int, val minY: () -> Int, val maxY: () -> Int): Filter<T>({
    it.x in minX()..maxX() && it.y in minY()..maxY()
})