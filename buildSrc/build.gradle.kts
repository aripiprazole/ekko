plugins {
  `kotlin-dsl`
}

repositories {
  maven("https://plugins.gradle.org/m2/")
}

dependencies {
  implementation("org.jlleitschuh.gradle:ktlint-gradle:10.3.0")
  implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.21.0")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
}
