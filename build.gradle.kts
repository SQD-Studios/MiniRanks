plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("com.gradleup.shadow") version "9.6.1"
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
    maven("https://nexus.scarsz.me/content/groups/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    implementation("net.chamosmp.sqdlib:sqdlib-paper:2.+")

    compileOnly("net.strokkur.commands:annotations-paper:2.3.0")
    annotationProcessor("net.strokkur.commands:processor-paper:2.3.0")

    compileOnly("me.clip:placeholderapi:2.12.3")
    compileOnly("net.luckperms:api:5.5")
}

tasks {
    runServer {
        minecraftVersion("26.2")

        downloadPlugins {
            modrinth("Vebnzrzj", "v5.5.71-bukkit") // LuckPerms
            modrinth("lKEzGugV", "2.12.3") // PlaceholderAPI
        }
    }
    runPaper.folia.registerTask()

    shadowJar {
        configurations = project.configurations.runtimeClasspath.map { setOf(it) }

        relocate("net.chamosmp.sqdlib", "net.chamosmp.miniranks.libs.sqdlib")
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