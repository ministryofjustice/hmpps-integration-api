import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.1.0"
  kotlin("plugin.spring") version "2.1.20"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
  implementation("io.sentry:sentry-spring-boot-starter-jakarta:8.11.1")
  implementation("io.sentry:sentry-logback:8.11.1")
  implementation("org.springframework.data:spring-data-commons")
  implementation("org.springframework:spring-aop")
  implementation("org.aspectj:aspectjweaver")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.4.4") {
    exclude("org.springframework.security", "spring-security-config")
    exclude("org.springframework.security", "spring-security-core")
    exclude("org.springframework.security", "spring-security-crypto")
    exclude("org.springframework.security", "spring-security-web")
  }
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8") {
    constraints {
      implementation("org.webjars:swagger-ui:5.21.0") // Fix security build HMAI-317
    }
  }
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  testImplementation("io.kotest:kotest-assertions-json-jvm:5.9.1")
  testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
  testImplementation("io.kotest:kotest-assertions-core-jvm:5.9.1")
  testImplementation("org.wiremock:wiremock-standalone:3.13.0")
  testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
  testImplementation("org.mockito:mockito-core:5.17.0")
  testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")

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
      excludeTestsMatching("uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration*")
    }
  }

  register<Test>("integrationTest") {
    description = "Runs the integration tests, make sure that dependencies are available first by running `make serve`."
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["main"].output + configurations["testRuntimeClasspath"] + sourceSets["test"].output
    filter {
      includeTestsMatching("uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration*")
    }
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_21
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
