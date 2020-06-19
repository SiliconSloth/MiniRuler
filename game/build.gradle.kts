plugins {
    application
}

sourceSets.main {
    java.srcDirs("src")
    resources.srcDirs("res")
}

application {
    mainClassName = "com.mojang.ld22.Game"
}
