import kotlinx.kover.gradle.plugin.dsl.tasks.KoverReport
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.1.2"
  kotlin("plugin.spring") version "2.2.20"
  id("io.gitlab.arturbosch.detekt") version "1.23.8"
  id("org.jetbrains.kotlinx.kover") version "0.9.2"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
  runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
  implementation("io.sentry:sentry-spring-boot-starter-jakarta:8.23.0")
  implementation("io.sentry:sentry-logback:8.23.0")
  implementation("org.springframework.data:spring-data-commons")
  implementation("org.springframework:spring-aop")
  implementation("org.aspectj:aspectjweaver")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.4.11") {
    exclude("org.springframework.security", "spring-security-config")
    exclude("org.springframework.security", "spring-security-core")
    exclude("org.springframework.security", "spring-security-crypto")
    exclude("org.springframework.security", "spring-security-web")
  }
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation("io.github.microutils:kotlin-logging:3.0.5")
  implementation("io.jsonwebtoken:jjwt-api:0.13.0")
  testImplementation("io.kotest:kotest-assertions-json-jvm:6.0.3")
  testImplementation("io.kotest:kotest-runner-junit5-jvm:6.0.3")
  testImplementation("io.kotest:kotest-assertions-core-jvm:6.0.3")
  testImplementation("io.kotest:kotest-extensions-spring:6.0.3")
  implementation("org.jetbrains.kotlinx:kover-cli:0.9.1")
  testImplementation("org.wiremock:wiremock-standalone:3.13.1")
  testImplementation("org.mockito:mockito-core:5.20.0")
  testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.0")
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
  testImplementation("com.atlassian.oai:swagger-request-validator-wiremock:2.46.0") {
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
  testImplementation("org.eclipse.jetty:jetty-util:12.1.1")
  testImplementation("org.eclipse.jetty:jetty-server:12.1.1")
  testImplementation("org.eclipse.jetty:jetty-http:12.1.1")
  testImplementation("org.eclipse.jetty:jetty-io:12.1.1")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  testImplementation(kotlin("test"))
  testImplementation("io.mockk:mockk:1.14.6")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
  mavenCentral()
}

tasks {
  // Enables the coverage report to be created for only unit tests or integration tests
  // This is so the unit and integration tests can be run in parallel
  // Could have just called
  withType<KoverReport>().configureEach {
    val environment = System.getenv()
    val testType = environment["TEST_TYPE"] ?: "UNIT"
    val excluded = if (testType == "UNIT") "integrationTest" else "unitTest"
    kover {
      currentProject {
        instrumentation {
          disabledForTestTasks.add(excluded)
          disabledForTestTasks.add("test")
        }
      }
    }
  }

  val mergeCoverageReport by registering(JavaExec::class) {
    classpath = files("lib/kover-cli.jar")
    args =
      listOf(
        "merge",
        "lib/unitTest.ic",
        "lib/integrationTest.ic",
        "--target=lib/merged.ic",
      )
  }

  val createCoverageReport by registering(JavaExec::class) {
    classpath = files("lib/kover-cli.jar")
    args =
      listOf(
        "report",
        "lib/merged.ic",
        "--src=src/main",
        "--classfiles=build/classes/kotlin/main",
        "--html=coverage",
        "--xml=coverage/report.xml",
      )
  }

  val koverCli by registering(Copy::class) {
    from(configurations.runtimeClasspath).include("kover-cli*.jar")
    into("lib")
    rename("(.*).jar", "kover-cli.jar")
  }
  getByName("mergeCoverageReport").dependsOn(koverCli)
  getByName("createCoverageReport").dependsOn(mergeCoverageReport)

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

  withType<Test> {
    systemProperty("kotest.framework.config.fqn", "uk.gov.justice.digital.hmpps.hmppsintegrationapi.kotest.ProjectConfig")
  }

  withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    source = source.asFileTree
  }

  getByName("check") {
    dependsOn(":ktlintCheck", "detekt")
  }
}

detekt {
  config.setFrom("./detekt.yml")
  buildUponDefaultConfig = true
  ignoreFailures = true
  baseline = file("./detekt-baseline.xml")
}

kotlin {
  kotlinDaemonJvmArgs = listOf("-Xmx2g")
}

testlogger {
  theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
}

// this is to address JLLeitschuh/ktlint-gradle#809
ktlint {
  version = "1.5.0"
}

configurations.matching { it.name == "detekt" }.all {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.jetbrains.kotlin") {
      useVersion(
        io.gitlab.arturbosch.detekt
          .getSupportedKotlinVersion(),
      )
    }
  }
}
