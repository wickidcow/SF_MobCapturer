plugins {
    id("java")
    id("maven-publish")
    id("io.freefair.lombok") version "8.7.1"
    id("com.gradleup.shadow") version "9.3.0"
    id("de.eldoria.plugin-yml.bukkit") version "0.8.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.slimefunguguproject:Slimefun4:5c188a3c0a")
    compileOnly("net.guizhanss:GuizhanLibPlugin:2.3.0")
    implementation("org.bstats:bstats-bukkit:3.0.3")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
}

group = "io.github.thebusybiscuit"
version = "UNOFFICIAL"
description = "MobCapturer"

java {
    disableAutoTargetJvm()
    sourceCompatibility = JavaVersion.VERSION_17
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    fun doRelocate(from: String) {
        val last = from.split(".").last()
        relocate(from, "io.github.thebusybiscuit.mobcapturer.libs.$last")
    }
    doRelocate("org.bstats")
    doRelocate("javax.annotation")
    doRelocate("io.papermc.paperlib")
    minimize()
    archiveClassifier = ""
}

bukkit {
    main = "io.github.thebusybiscuit.mobcapturer.MobCapturer"
    apiVersion = "1.18"
    authors = listOf("TheBusyBiscuit", "ybw0014")
    description = "A Slimefun Addon that adds a tool that allows you to capture mobs"
    website = "https://github.com/SlimefunGuguProject/MobCapturer"
    depend = listOf("Slimefun")
    softDepend = listOf("GuizhanLibPlugin")
}

tasks {
    runServer {
        downloadPlugins {
            // Slimefun
            url("https://builds.guizhanss.com/api/download/SlimefunGuguProject/Slimefun4/master/latest")
            // GuizhanLibPlugin
            url("https://builds.guizhanss.com/api/download/ybw0014/GuizhanLibPlugin/master/latest")
            // SlimeHUD
            url("https://builds.guizhanss.com/api/download/SlimefunGuguProject/SlimeHUD/master/latest")
            // GuizhanCraft for testing convenient
            url("https://builds.guizhanss.com/api/download/ybw0014/GuizhanCraft/master/latest")
        }
        jvmArgs("-Dcom.mojang.eula.agree=true")
        minecraftVersion("1.21.11")
    }
}
