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

java {
  targetCompatibility = JavaVersion.VERSION_1_8
  sourceCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation("com.strumenta.antlr-kotlin:antlr-kotlin-runtime-jvm:160bc0b70f")
}

val generateMainAntlrSource by tasks.creating(AntlrKotlinTask::class) {
  antlrClasspath = configurations.detachedConfiguration(
    project.dependencies.create("org.antlr:antlr4:4.7.1"),
    project.dependencies.create("com.strumenta.antlr-kotlin:antlr-kotlin-target:160bc0b70f"),
  )
  maxHeapSize = "64m"
  arguments = listOf("-package", "ekko.parsing")
  source = project.objects
    .sourceDirectorySet("antlr", "antlr")
    .srcDir("src/main/antlr").apply {
      include("*.g4")
    }

  // Temporary folder for antlr generated files to be copied with `generateAntlrSource` task suppressing the warnings
  outputDirectory = buildDir.resolve("build/generated/kotlin")
}

// Workaround to suppress Kotlin, Detekt and Ktlint warnings in generated parser source.
// The original issue is at https://github.com/Strumenta/antlr-kotlin/issues/36.
val generateAntlrSource by tasks.creating(Copy::class) {
  dependsOn(generateMainAntlrSource)
  from(buildDir.resolve("build/generated/kotlin")) // The previous configured temporary folder
  include("**/*.kt")
  filter<KtSuppressFilterReader>()
  into(rootProject.file("src/generated/kotlin"))
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  // Makes the kotlin compile task depends on `generateAntlrSource` task, to be easier to set up a CI pipeline, or even
  // build the project
  dependsOn(generateAntlrSource)
  kotlinOptions.jvmTarget = "1.8"
}
