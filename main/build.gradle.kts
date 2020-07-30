plugins {
    kotlin("jvm") version "1.3.72"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":game"))
}

tasks.register<JavaExec>("run") {
    dependsOn("classes")
    classpath = sourceSets.main.get().runtimeClasspath
    main = "siliconsloth.miniruler.MainKt"
    jvmArgs = listOf("-ea")
}
