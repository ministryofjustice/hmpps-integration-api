plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "7.1.3"
  kotlin("plugin.spring") version "2.1.10"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  implementation("org.springframework.boot:spring-boot-starter-webflux:3.4.3")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
  implementation("io.sentry:sentry-spring-boot-starter-jakarta:8.3.0")
  implementation("io.sentry:sentry-logback:8.3.0")
  implementation("org.springframework.data:spring-data-commons:3.4.3")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.3.2") {
    exclude("org.springframework.security", "spring-security-config")
    exclude("org.springframework.security", "spring-security-core")
    exclude("org.springframework.security", "spring-security-crypto")
    exclude("org.springframework.security", "spring-security-web")
  }
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0") {
    constraints {
      implementation("org.webjars:swagger-ui:5.20.0") // Fix security build HMAI-317
    }
  }
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
  testImplementation("io.kotest:kotest-assertions-json-jvm:5.9.1")
  testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
  testImplementation("io.kotest:kotest-assertions-core-jvm:5.9.1")
  testImplementation("org.wiremock:wiremock-standalone:3.12.1")
  testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
  testImplementation("org.mockito:mockito-core:5.16.0")
  testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  testImplementation(kotlin("test"))
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
repositories {
  mavenCentral()
}

tasks {
  register<Test>("unitTest") {
    filter {
      excludeTestsMatching("uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke*")
      excludeTestsMatching("uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration*")
    }
  }

  register<Test>("smokeTest") {
    filter {
      includeTestsMatching("uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke*")
    }
  }

  register<Test>("integrationTest") {
    filter {
      includeTestsMatching("uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration*")
    }
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
}

testlogger {
  theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
}

// this is to address JLLeitschuh/ktlint-gradle#809
ktlint {
  version = "1.5.0"
}
