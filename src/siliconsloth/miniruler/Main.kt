package siliconsloth.miniruler

import org.kie.api.KieServices

fun main(args: Array<String>) {
    val ks = KieServices.Factory.get()
    val kContainer = ks.kieClasspathContainer
    val kSession = kContainer.newKieSession("ksession-rules")

    kSession.fireAllRules()
}