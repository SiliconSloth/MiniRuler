package siliconsloth.miniruler.engine.filters

import siliconsloth.miniruler.Spatial
import siliconsloth.miniruler.math.Box

class AreaFilter<T: Spatial>(val box: () -> Box): Filter<T>({
    it.pos in box()
})