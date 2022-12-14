plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.5.7"
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
