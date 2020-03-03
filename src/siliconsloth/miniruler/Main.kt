package siliconsloth.miniruler

import com.mojang.ld22.Game
import kotlinx.coroutines.runBlocking
import org.kie.api.KieServices
import org.kie.api.runtime.KieSession
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.rules.memoryRules
import siliconsloth.miniruler.rules.menuRules
import java.lang.Exception

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
    }
}

fun <T: Spatial> screenFilter(cameraX: () -> Int, cameraY: () -> Int) = AreaFilter<T>(
        { (cameraX() / 16) * 16 },
        { ((cameraX() + Game.WIDTH) / 16) * 16 - 1 },
        { (cameraY() / 16) * 16 },
        { ((cameraY() + Game.HEIGHT) / 16) * 16 - 1 }
)

fun KieSession.update(fact: Fact) =
        update(getFactHandle(fact), fact)

fun KieSession.delete(fact: Fact) =
        try {
            delete(getFactHandle(fact))
        } catch (e: Exception) {

        }

fun KieSession.insertOrUpdate(fact: Fact) {
    if (getFactHandle(fact) == null) {
        insert(fact)
    } else {
        update(fact)
    }
}

fun KieSession.deleteIfPresent(fact: Fact) {
    if (getFactHandle(fact) != null) {
        delete(fact)
    }
}