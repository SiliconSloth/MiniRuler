plugins {
    kotlin("jvm") version "1.3.72"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.beust:klaxon:5.2")
}

tasks.register<JavaExec>("run") {
    dependsOn("classes")
    classpath = sourceSets.main.get().runtimeClasspath
    workingDir = rootDir.resolve("run")
    main = "siliconsloth.miniruler.timelineviewer.TimelineViewerKt"
    jvmArgs = listOf("-ea")
}

