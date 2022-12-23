plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.7.4"
  kotlin("plugin.spring") version "1.7.20"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")

  testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.4")
  testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.4")
  testImplementation("io.kotest.extensions:kotest-extensions-wiremock:1.0.3")
  testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
  testImplementation("org.mockito:mockito-core:4.9.0")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(18))
}

tasks {
  withType<Test>().configureEach {
    useJUnitPlatform()
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
