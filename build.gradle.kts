import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("com.gradleup.shadow") version "9.6.1"

    id("io.papermc.hangar-publish-plugin") version "0.1.4"
    id("com.modrinth.minotaur") version "2.+"
}

group = "net.chamosmp"
version = "1.0.0"

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "chamosmpRepoReleases"
        url = uri("https://maven.chamosmp.net/releases")
    }
    maven {
        name = "Eldonexus"
        url = uri("https://eldonexus.de/repository/maven-public/")
    }
    maven {
        name = "PlaceholderAPI"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    implementation("net.chamosmp.sqdlib:sqdlib-paper:2.+")

    compileOnly("net.strokkur.commands:annotations-paper:2.3.0")
    annotationProcessor("net.strokkur.commands:processor-paper:2.3.0")

    compileOnly("me.clip:placeholderapi:2.12.3")
}

tasks {
    runServer {
        minecraftVersion("26.2")
    }
    runPaper.folia.registerTask()

    shadowJar {
        configurations = project.configurations.runtimeClasspath.map { setOf(it) }

        relocate("net.chamosmp.sqdlib", "net.chamosmp.(plugin).libs.sqdlib")
    }

    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"

        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:deprecation")
    }
}

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

hangarPublish {
    publications.register("plugin") {
        version.set(project.version as String)
        channel.set("Release")
        id.set("MiniLobby")
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set(listOf("1.21-26.2"))

                dependencies {
                    hangar("PlaceholderAPI") {
                        required.set(false)
                    }
                }
            }
        }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("hUJyv10y")
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(
        "1.21",
        "1.21.1",
        "1.21.2",
        "1.21.3",
        "1.21.4",
        "1.21.5",
        "1.21.6",
        "1.21.7",
        "1.21.8",
        "1.21.9",
        "1.21.10",
        "1.21.11",
        "26.1.2",
        "26.1",
        "26.1.1",
        "26.2"
    )
    loaders.addAll("folia", "paper", "purpur")
    dependencies {
        optional.project("lKEzGugV") // PlaceholderAPI
    }
}

tasks.register("publishToAllPlatforms") {
    group = "publishing"
    description = "Publishes all platforms"
    dependsOn("modrinth")
    dependsOn("publishAllPublicationsToHangar")
}