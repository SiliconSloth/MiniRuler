plugins {
    kotlin("jvm") version "1.3.72"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":game"))
    implementation("it.uniroma1.dis.wsngroup.gexf4j:gexf4j:1.0.0")
    implementation("com.beust:klaxon:5.2")
}

tasks.register<JavaExec>("run") {
    dependsOn("classes")
    classpath = sourceSets.main.get().runtimeClasspath
    workingDir = rootDir.resolve("run")
    main = "siliconsloth.miniruler.MainKt"
    jvmArgs = listOf("-ea")
}

tasks.register<JavaExec>("viewer") {
    dependsOn("classes")
    classpath = sourceSets.main.get().runtimeClasspath
    workingDir = rootDir.resolve("run")
    main = "siliconsloth.miniruler.engine.recorder.TimelineViewerKt"
    jvmArgs = listOf("-ea")
}
