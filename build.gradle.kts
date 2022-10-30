import ekko.gradle.AntlrPackagingTask
import ekko.gradle.PACKAGE_FILE_HEADER
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  antlr
  java
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

ktlint {
  version.set("0.47.1")
}

val mainAntlrOutputDirectory = buildDir.resolve("generated-src/antlr/main")
val antlrOutputDirectory = buildDir.resolve("generated-src/antlr")

kotlin {
  sourceSets {
    main {
      kotlin.srcDirs(mainAntlrOutputDirectory)
    }
  }
}

java {
  targetCompatibility = JavaVersion.VERSION_16
  sourceCompatibility = JavaVersion.VERSION_16
}

dependencies {
  antlr("org.antlr:antlr4:4.11.1")
  implementation("com.github.ajalt.mordant:mordant:2.0.0-beta7")
}

tasks {
  generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages")
    outputDirectory = antlrOutputDirectory.resolve("temp")
  }

  val generateParserSource by creating(Copy::class) {
    from(generateGrammarSource)
    into(mainAntlrOutputDirectory.resolve(PACKAGE_FILE_HEADER))
    include("**/*.java")
    filter<AntlrPackagingTask>()
  }

  test {
    useJUnitPlatform()
  }

  withType<KotlinCompile> {
    dependsOn(generateParserSource)
    kotlinOptions.jvmTarget = "16"
    kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
  }
}
