package siliconsloth.miniruler

import com.mojang.ld22.Game
import org.kie.api.KieServices

fun main(args: Array<String>) {
    val ks = KieServices.Factory.get()
    val kContainer = ks.kieClasspathContainer
    val kSession = kContainer.newKieSession("ksession-rules")

    val game = Game.startWindowedGame(PerceptionHandler(kSession))

    kSession.fireUntilHalt()
}