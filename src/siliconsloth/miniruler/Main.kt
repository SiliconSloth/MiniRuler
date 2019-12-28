package siliconsloth.miniruler

import com.mojang.ld22.Game
import org.kie.api.KieServices
import org.kie.api.runtime.KieSession

fun main() {
    val kServices = KieServices.Factory.get()
    val kContainer = kServices.kieClasspathContainer
    val kSession = kContainer.newKieSession("ksession-rules")

    val game = Game.startWindowedGame(PerceptionHandler(kSession))

    kSession.addEventListener(KeyListener(game.input))
    kSession.fireUntilHalt()
}

fun KieSession.update(fact: Fact) =
        update(getFactHandle(fact), fact)

fun KieSession.delete(fact: Fact) =
        delete(getFactHandle(fact))