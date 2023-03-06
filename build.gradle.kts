plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.1.0"
  kotlin("plugin.spring") version "1.8.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

  runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")
  testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.4")
  testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.4")
  testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")
  testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
  testImplementation("org.mockito:mockito-core:4.9.0")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(18))
}

tasks {
  register<Test>("unitTest") {
    filter {
      excludeTestsMatching("uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke*")
    }
  }

  register<Test>("smokeTest") {
    filter {
      includeTestsMatching("uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke*")
    }
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "18"
    }
  }
}

testlogger {
  theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
}
