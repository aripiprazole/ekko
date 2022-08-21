import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.7.10"
  id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
  id("io.gitlab.arturbosch.detekt") version "1.19.0"
}

group = "me.devgabi"
version = "0.0.1"

repositories {
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

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
  useJUnitPlatform()
}
