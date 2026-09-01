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
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://maven.norain.city/releases")
    maven("https://maven.norain.city/snapshots")
    maven("https://jitpack.io")
}

val slimefunLegacyJar = file("libs/Slimefun-Legacy.jar")
val platform = providers.gradleProperty("platform").orElse("paper").get().lowercase()
val platformVersion = providers.gradleProperty("platformVersion").orElse(
    if (platform == "folia") "26.2.build.+" else "1.21.11-R0.1-SNAPSHOT"
).get()
val platformApi = when (platform) {
    "paper" -> "io.papermc.paper:paper-api:$platformVersion"
    "purpur" -> "org.purpurmc.purpur:purpur-api:$platformVersion"
    "folia" -> "dev.folia:folia-api:$platformVersion"
    else -> error("Unsupported platform '$platform'. Use paper, purpur, or folia.")
}

dependencies {
    compileOnly(platformApi)

    if (slimefunLegacyJar.exists()) {
        compileOnly(files(slimefunLegacyJar))
    } else {
        compileOnly("com.github.slimefunguguproject:Slimefun4:5c188a3c0a")
    }

    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")

    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
}

group = "io.github.wickidcow"
version = "1.0.2"
description = "MobCapturer for Slimefun Legacy on Minecraft 1.21.11 through 26.2"

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
    apiVersion = "1.21.11"
    foliaSupported = true
    authors = listOf("TheBusyBiscuit", "ybw0014", "wickidcow")
    description = "A Slimefun Legacy addon that adds reusable mob capture tools"
    website = "https://github.com/wickidcow/SF_MobCapturer"
    depend = listOf("Slimefun")
    softDepend = listOf("EliteMobs", "MythicMobs")
}
