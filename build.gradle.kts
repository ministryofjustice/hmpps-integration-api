import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.3.1"
  kotlin("plugin.spring") version "2.2.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
  implementation("io.sentry:sentry-spring-boot-starter-jakarta:8.17.0")
  implementation("io.sentry:sentry-logback:8.17.0")
  implementation("org.springframework.data:spring-data-commons")
  implementation("org.springframework:spring-aop")
  implementation("org.aspectj:aspectjweaver")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.4.7") {
    exclude("org.springframework.security", "spring-security-config")
    exclude("org.springframework.security", "spring-security-core")
    exclude("org.springframework.security", "spring-security-crypto")
    exclude("org.springframework.security", "spring-security-web")
  }
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation("io.github.microutils:kotlin-logging:3.0.5")
  testImplementation("io.kotest:kotest-assertions-json-jvm:5.9.1")
  testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
  testImplementation("io.kotest:kotest-assertions-core-jvm:5.9.1")
  testImplementation("org.wiremock:wiremock-standalone:3.13.1")
  testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
  testImplementation("org.mockito:mockito-core:5.18.0")
  testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.1")
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
  testImplementation("com.atlassian.oai:swagger-request-validator-wiremock:2.44.9") {
    // Exclude WireMock artifacts
    exclude(group = "com.github.tomakehurst", module = "wiremock")
    exclude(group = "com.github.tomakehurst", module = "wiremock-jre8")
    exclude(group = "com.github.tomakehurst", module = "wiremock-standalone")

    // Exclude Jetty components to prevent the validator from pulling in conflicting versions
    exclude(group = "org.eclipse.jetty")
    exclude(group = "javax.servlet")
  }
  // Explicitly add all necessary Jetty and Servlet dependencies
  testImplementation("javax.servlet:javax.servlet-api:4.0.1")
  testImplementation("org.eclipse.jetty:jetty-util:12.0.12")
  testImplementation("org.eclipse.jetty:jetty-server:12.0.12")
  testImplementation("org.eclipse.jetty:jetty-http:12.0.12")
  testImplementation("org.eclipse.jetty:jetty-io:12.0.12")

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
    group = "verification"
    filter {
      excludeTestsMatching("uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration*")
    }
  }

  register<Test>("integrationTest") {
    description = "Runs the integration tests, make sure that dependencies are available first by running `make serve`."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["main"].output + configurations["testRuntimeClasspath"] + sourceSets["test"].output
    filter {
      includeTestsMatching("uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration*")
    }
  }

  withType<KotlinCompile> {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_21
      freeCompilerArgs.add("-Xannotation-default-target=param-property")
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
