import com.strumenta.antlrkotlin.gradleplugin.AntlrKotlinTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  repositories {
    maven("https://jitpack.io")
    mavenCentral()
  }

  dependencies {
    classpath("com.strumenta.antlr-kotlin:antlr-kotlin-gradle-plugin:160bc0b70f")
  }
}

plugins {
  kotlin("jvm") version "1.7.10"
  id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
  id("io.gitlab.arturbosch.detekt") version "1.19.0"
}

group = "me.devgabi"
version = "0.0.1"

repositories {
  maven("https://jitpack.io")
  mavenCentral()
}

ktlint {
  android.set(false)
  additionalEditorconfigFile.set(rootProject.file(".editorconfig"))
}

detekt {
  buildUponDefaultConfig = true
  allRules = false

  config = files("${rootProject.projectDir}/config/detekt.yml")
  baseline = file("${rootProject.projectDir}/config/baseline.xml")
}

kotlin {
  sourceSets {
    main {
      kotlin.srcDirs(rootProject.file("src/generated/kotlin"))
    }
  }
}

dependencies {
  implementation("com.strumenta.antlr-kotlin:antlr-kotlin-runtime-jvm:160bc0b70f")
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

val generateParserSource by tasks.creating(AntlrKotlinTask::class) {
  val dependencies = project.dependencies

  antlrClasspath = configurations.detachedConfiguration(
    dependencies.create("org.antlr:antlr4:4.7.1"),
    dependencies.create("com.strumenta.antlr-kotlin:antlr-kotlin-target:160bc0b70f"),
  )
  maxHeapSize = "64m"
  arguments = listOf("-package", "ekko.parser")
  source = project.objects
    .sourceDirectorySet("antlr", "antlr")
    .srcDir("src/main/antlr").apply {
      include("*.g4")
    }
  outputDirectory = rootProject.file("src/generated/kotlin")
}
