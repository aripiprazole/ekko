import com.strumenta.antlrkotlin.gradleplugin.AntlrKotlinTask
import ekko.gradle.KtSuppressFilterReader
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
  kotlin("jvm")
  id("org.jlleitschuh.gradle.ktlint")
  id("io.gitlab.arturbosch.detekt")
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

val generateMainAntlrSource by tasks.creating(AntlrKotlinTask::class) {
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
  outputDirectory = buildDir.resolve("build/generated/kotlin")
}

/**
 * Workaround to suppress all warnings in generated parser source.
 *
 * Original issue is [here](https://github.com/Strumenta/antlr-kotlin/issues/36).
 */
val generateAntlrSource by tasks.creating(Copy::class) {
  dependsOn(generateMainAntlrSource)
  from(buildDir.resolve("build/generated/kotlin"))
  include("**/*.kt")
  filter<KtSuppressFilterReader>()
  into(rootProject.file("src/generated/kotlin"))
}
