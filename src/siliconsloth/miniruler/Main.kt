package siliconsloth.miniruler

import com.mojang.ld22.Game
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.stores.SpatialMap
import siliconsloth.miniruler.math.Box
import siliconsloth.miniruler.math.Vector
import siliconsloth.miniruler.rules.attackRules
import siliconsloth.miniruler.rules.memoryRules
import siliconsloth.miniruler.rules.menuRules
import siliconsloth.miniruler.rules.navigationRules

fun main() {
    val engine = RuleEngine()
    engine.addFactStore(SpatialMap<Memory>())

    val game = Game.startWindowedGame(PerceptionHandler(engine))
    KeyListener(engine, game.botInput)
    Visualizer(engine).display()

    engine.menuRules()
    engine.memoryRules()
    engine.navigationRules()
    engine.attackRules()
}

fun <T: Spatial> screenFilter(camera: () -> Vector) = AreaFilter<T> { Box(
        (camera() / 16) * 16 + 8,
        ((camera() + Vector(Game.WIDTH, Game.HEIGHT)) / 16) * 16 + 7
) }