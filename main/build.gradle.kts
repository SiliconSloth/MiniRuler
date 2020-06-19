plugins {
    kotlin("jvm") version "1.3.72"
    application
}

repositories {
    jcenter()
}

application {
    mainClassName = "siliconsloth.miniruler.MainKt"
    applicationDefaultJvmArgs = listOf("-ea")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":game"))
    implementation("it.uniroma1.dis.wsngroup.gexf4j:gexf4j:1.0.0")
    implementation("com.beust:klaxon:5.2")
}

tasks.register<JavaExec>("viewer") {
    dependsOn("classes")
    classpath = sourceSets.main.get().runtimeClasspath
    main = "siliconsloth.miniruler.engine.recorder.TimelineViewerKt"
    jvmArgs = listOf("-ea")
}
