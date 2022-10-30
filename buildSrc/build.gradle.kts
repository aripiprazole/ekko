import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `kotlin-dsl`
}

repositories {
  maven("https://plugins.gradle.org/m2/")
}

dependencies {
  implementation("org.jlleitschuh.gradle:ktlint-gradle:11.0.0")
  implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.19.0")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
}

java {
  targetCompatibility = JavaVersion.VERSION_16
  sourceCompatibility = JavaVersion.VERSION_16
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "16"
}
