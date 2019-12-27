package siliconsloth.miniruler

import com.mojang.ld22.Game
import org.kie.api.KieServices

fun main(args: Array<String>) {
    val kServices = KieServices.Factory.get()
    val kContainer = kServices.kieClasspathContainer
    val kSession = kContainer.newKieSession("ksession-rules")

    val game = Game.startWindowedGame(PerceptionHandler(kSession))

    kSession.addEventListener(KeyListener(game.input))
    kSession.fireUntilHalt()
}