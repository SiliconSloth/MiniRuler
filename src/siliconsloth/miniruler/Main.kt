package siliconsloth.miniruler

import com.mojang.ld22.Game
import org.kie.api.KieServices
import org.kie.api.runtime.KieSession
import java.lang.Exception

fun main() {
    val kServices = KieServices.Factory.get()
    val kContainer = kServices.kieClasspathContainer
    val kSession = kContainer.newKieSession("ksession-rules")

    val spatialMemory = SpatialMemoryStore(kSession)
    kSession.addEventListener(spatialMemory)
    kSession.insert(spatialMemory)

    val game = Game.startWindowedGame(PerceptionHandler(kSession))
    kSession.addEventListener(KeyListener(game.input))

    val visualizer = Visualizer(spatialMemory)
//    kSession.addEventListener(visualizer)
    visualizer.display()

    kSession.fireUntilHalt()
}

fun onScreen(tileX: Int, tileY: Int, cameraX: Int, cameraY: Int): Boolean =
        tileX >= (cameraX / 16) * 16 &&
        tileX < ((cameraX + Game.WIDTH) / 16) * 16 &&
        tileY >= (cameraY / 16) * 16 &&
        tileY < ((cameraY + Game.HEIGHT) / 16) * 16

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