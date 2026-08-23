import org.gradle.api.attributes.java.TargetJvmVersion

plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.3.2"
    id("de.eldoria.plugin-yml.bukkit") version "0.8.0"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.norain.city/releases")
    maven("https://maven.norain.city/snapshots")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+") {
        attributes {
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
        }
    }
    compileOnly("com.github.slimefun:Slimefun:4.1.37")
    compileOnly("net.guizhanss:GuizhanLibPlugin:2.5.0")

    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")

    implementation("org.bstats:bstats-bukkit:3.0.3")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
}

group = "io.github.wickidcow"
version = "1.0.0"
description = "MobCapturer for Slimefun Legacy"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.javadoc {
    options.encoding = "UTF-8"
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.shadowJar {
    fun doRelocate(from: String) {
        val last = from.split(".").last()
        relocate(from, "io.github.thebusybiscuit.mobcapturer.libs.$last")
    }

    doRelocate("org.bstats")
    doRelocate("javax.annotation")
    doRelocate("io.papermc.paperlib")

    archiveClassifier.set("")
    archiveFileName.set("SF_MobCapturer${project.version}.jar")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

bukkit {
    main = "io.github.thebusybiscuit.mobcapturer.MobCapturer"
    apiVersion = "1.21"
    authors = listOf("TheBusyBiscuit", "ybw0014", "wickidcow")
    description = "A Slimefun Legacy addon that adds reusable mob capture tools"
    website = "https://github.com/wickidcow/SF_MobCapturer"
    depend = listOf("Slimefun")
    softDepend = listOf("GuizhanLibPlugin")
}
