plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
}

group = "gbovv.com"
version = "1.0.0"
description = "SNBPlugin"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("dev.folia:folia-api:1.21.4-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-Xnullability-annotations=@org.eclipse.jdt.annotation.NonNull"
            )
        }
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }
    jar {
        archiveFileName.set("SNBPlugin-${version}.jar")
    }
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
