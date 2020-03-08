package siliconsloth.miniruler

import com.mojang.ld22.Game
import kotlinx.coroutines.runBlocking
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.stores.SpatialStore
import siliconsloth.miniruler.rules.memoryRules
import siliconsloth.miniruler.rules.menuRules
import siliconsloth.miniruler.rules.navigationRules

fun main() {
    runBlocking {
        val engine = RuleEngine(this)
        engine.addFactStore(SpatialStore<TileMemory>())
        engine.addFactStore(SpatialStore<EntityMemory>())

        val game = Game.startWindowedGame(PerceptionHandler(engine))
        KeyListener(engine, game.input)
        Visualizer(engine).display()

        engine.menuRules()
        engine.memoryRules()
        engine.navigationRules()
    }
}

fun <T: Spatial> screenFilter(cameraX: () -> Int, cameraY: () -> Int) = AreaFilter<T>(
        { (cameraX() / 16) * 16 },
        { ((cameraX() + Game.WIDTH) / 16) * 16 - 1 },
        { (cameraY() / 16) * 16 },
        { ((cameraY() + Game.HEIGHT) / 16) * 16 - 1 }
)