plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "net.developerden"


repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
    maven("https://maven.scijava.org/content/groups/public/")
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2-native-mt")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.30")


    implementation("com.jagrosh:jda-utilities:3.0.5")
    implementation("net.dv8tion:JDA:4.3.0_330")
    implementation("com.github.MinnDevelopment:jda-reactor:1.3.0")

    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha5")
    implementation("io.ktor:ktor-client-core:1.6.3")
    implementation("io.ktor:ktor-client-cio:1.6.3")
    implementation("io.ktor:ktor-client-serialization:1.6.3")

    implementation("ch.qos.logback:logback-core:1.3.0-alpha10")
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha10")

    implementation("io.github.microutils:kotlin-logging:2.0.11")

    implementation("io.github.classgraph:classgraph:4.8.116")
    implementation("com.google.inject:guice:5.0.1")
    implementation("dev.misfitlabs.kotlinguice4:kotlin-guice:1.5.0")

    implementation("info.debatty:java-string-similarity:2.0.0")

    implementation("io.sentry:sentry:5.1.2")

    implementation("com.zaxxer:HikariCP:5.0.0")
    implementation("org.jetbrains.exposed", "exposed-core", "0.31.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.31.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.31.1")
    implementation("org.jetbrains.exposed", "exposed-java-time", "0.31.1")
    implementation("mysql:mysql-connector-java:8.0.26")
    implementation("com.h2database:h2:1.4.200")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.0")

    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
}


tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnit()
    }

    application {
        // Define the main class for the application.
        mainClass.set("net.developerden.devdenbot.AppKt")
        applicationName = "DevDenBot"
    }

    shadowJar {
        isZip64 = true
    }

    jar {
        enabled = false
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        from("${project.rootDir}/src/main/resources/version.txt") {
            expand("version" to project.version)
        }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
