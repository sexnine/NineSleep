import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.BufferedReader

plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.0"
}

var commitHash: String by extra
commitHash = Runtime
    .getRuntime()
    .exec("git rev-parse --short HEAD")
    .let { process ->
        process.waitFor()
        val output = process.inputStream.use {
            it.bufferedReader().use(BufferedReader::readText)
        }
        process.destroy()
        output.trim()
    }

val baseName: String by project
val baseGroup: String by project
val baseVersion: String by project
val minimumApiVersion: String by project
val kotlinVersion: String by project
val basePackage = "$baseGroup.${baseName.lowercase()}"

group = baseGroup
version = "$baseVersion-$commitHash"

// Set up configurations for Shadow to use.
val includeAll = configurations.create("includeAll")

val includeNonLibraryLoader = configurations.create("includeNonLibraryLoader").apply {
    extendsFrom(includeAll)
}

val implementationConfiguration = configurations.getByName("implementation").apply {
    extendsFrom(includeNonLibraryLoader)
}


repositories {
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://oss.sonatype.org/content/repositories/central")
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven(url = "https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("net.lapismc:AFKPlus:3.4.1")
    includeNonLibraryLoader("net.kyori:adventure-text-minimessage:4.17.0")
    includeNonLibraryLoader("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    includeNonLibraryLoader("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    testImplementation(kotlin("test"))
}

val fullJar: TaskProvider<ShadowJar> = tasks.register<ShadowJar>("fullJar") {
    from(sourceSets.main.get().output)
    archiveClassifier.set("legacy")
    configurations = listOf(includeNonLibraryLoader)
}

val mainJar: TaskProvider<ShadowJar> = tasks.register<ShadowJar>("mainJar") {
    from(sourceSets.main.get().output)
    archiveClassifier.set("")
    configurations = listOf(includeAll)
}


// Fix placeholders in resource files (see plugin.yml)
tasks.processResources {
    expand(
        "baseName" to baseName,
        "baseGroup" to baseGroup,
        "basePackage" to basePackage,
        "baseVersion" to baseVersion,
        "commit" to commitHash,
        "version" to version,
        "kotlinVersion" to kotlinVersion,
        "apiVersion" to minimumApiVersion
    )
}

// Build both jars in the new "jars" task
tasks.register("jars") {
    dependsOn(setOf("fullJar", "mainJar"))
}

// Use the above jars task when we request a jar.
tasks.jar {
    dependsOn("jars")
}

tasks.register("jarDev") {
    version = "dev"
    dependsOn("jar")
}

// Fixes IntelliJ not getting test results.
tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}