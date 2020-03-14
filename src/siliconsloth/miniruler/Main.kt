package siliconsloth.miniruler

import com.mojang.ld22.Game
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.stores.SpatialStore
import siliconsloth.miniruler.rules.attackRules
import siliconsloth.miniruler.rules.memoryRules
import siliconsloth.miniruler.rules.menuRules
import siliconsloth.miniruler.rules.navigationRules

fun main() {
    val engine = RuleEngine()
    engine.addFactStore(SpatialStore<TileMemory>())
    engine.addFactStore(SpatialStore<EntityMemory>())

    val game = Game.startWindowedGame(PerceptionHandler(engine))
    KeyListener(engine, game.input)
    Visualizer(engine).display()

    engine.menuRules()
    engine.memoryRules()
    engine.navigationRules()
    engine.attackRules()
}

fun <T: Spatial> screenFilter(cameraX: () -> Int, cameraY: () -> Int) = AreaFilter<T>(
        { (cameraX() / 16) * 16 + 8 },
        { ((cameraX() + Game.WIDTH) / 16) * 16 + 7 },
        { (cameraY() / 16) * 16 + 8 },
        { ((cameraY() + Game.HEIGHT) / 16) * 16 + 7 }
)