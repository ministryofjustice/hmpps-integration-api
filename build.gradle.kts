import kotlinx.kover.gradle.plugin.dsl.tasks.KoverReport
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.5.3"
  kotlin("plugin.spring") version "2.4.0"
  id("dev.detekt") version "2.0.0-alpha.5"
  id("org.jetbrains.kotlinx.kover") version "0.9.8"
}

configurations.register("koverCli") {
  isCanBeConsumed = false
  isTransitive = true
  isCanBeResolved = true
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
  all {
    exclude(group = "dev.detekt", module = "detekt-report-checkstyle")
  }
}

configurations.all {
  resolutionStrategy.eachDependency {
    if (requested.group == ("org.apache.tomcat.embed")
      && requested.version == "11.0.22") {
      useVersion("11.0.23")
      because("Fix CVE-2026-55276, CVE-2026-53404, CVE-2026-55955, CVE-2026-55956, CVE-2026-50229, CVE-2026-53434")
    }
  }

  // CVE-2026-53914 - kotlin-build-tools-api-2.4.0.jar - needs 2.4.20 available
  // CVE-2026-54515 - jackson-databind needs 2.18.9, 2.21.5, and 3.1.4 - Waiting for spring boot starter
  // CVE-2020-29582 - jetbrains kotlin intellij-1 1.10.2 jar (issue with detekt aplha-5) Issue with detekt aplpha version
}

dependencies {
  runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
  runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.12")
  runtimeOnly("org.flywaydb:flyway-core")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  runtimeOnly("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
  implementation("io.sentry:sentry-spring-boot-4-starter:8.46.0")
  implementation("io.sentry:sentry-logback:8.46.0")
  implementation("org.springframework.data:spring-data-jdbc")
  implementation("org.springframework.data:spring-data-commons")
  implementation("org.springframework:spring-aop")
  implementation("org.aspectj:aspectjweaver")
  implementation("tools.jackson.module:jackson-module-kotlin:3.2.0")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.4.0") {
    exclude("org.springframework.security", "spring-security-config")
    exclude("org.springframework.security", "spring-security-core")
    exclude("org.springframework.security", "spring-security-crypto")
    exclude("org.springframework.security", "spring-security-web")
  }
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
  implementation("io.github.microutils:kotlin-logging:3.0.5")
  implementation("io.jsonwebtoken:jjwt-api:0.13.0")
  implementation("com.jayway.jsonpath:json-path:3.0.0")
  implementation("com.github.ben-manes.caffeine:caffeine:3.2.4")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("io.kotest:kotest-assertions-json-jvm:6.2.1")
  testImplementation("io.kotest:kotest-runner-junit5-jvm:6.2.1")
  testImplementation("io.kotest:kotest-assertions-core-jvm:6.2.1")
  testImplementation("io.kotest:kotest-extensions-spring:6.2.1")
  add("koverCli", "org.jetbrains.kotlinx:kover-cli:0.9.8")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("org.mockito:mockito-core:5.23.0")
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
  testImplementation("com.atlassian.oai:swagger-request-validator-wiremock:2.46.1") {
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
  testImplementation("org.eclipse.jetty:jetty-util:12.1.10")
  testImplementation("org.eclipse.jetty:jetty-server:12.1.10")
  testImplementation("org.eclipse.jetty:jetty-http:12.1.10")
  testImplementation("org.eclipse.jetty:jetty-io:12.1.10")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  testImplementation(kotlin("test"))
  testImplementation("io.mockk:mockk:1.14.11")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:5.1.2")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
  mavenCentral()
}

tasks {
  // Define the classes to be excluded from coverage reports.
  // ***APPROVAL** Please obtain approval from Service Technical Architect prior updating this list
  val classesToBeExcluded =
    arrayOf(
      "uk.gov.justice.digital.hmpps.hmppsintegrationapi.HmppsIntegrationApiKt",
      "uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.SchedulingConfig",
      "uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.DocumentationGenerator",
      "uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.FileManager",
    )

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
      reports {
        filters {
          excludes {
            classes(*classesToBeExcluded)
          }
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
      ) + classesToBeExcluded.map { "--exclude=$it" }
  }

  val koverCli by registering(Copy::class) {
    from(configurations.getByName("koverCli")).include("kover-cli*.jar")
    into("lib")
    rename("(.*).jar", "kover-cli.jar")
  }
  getByName("mergeCoverageReport").dependsOn(koverCli)
  getByName("createCoverageReport").dependsOn(mergeCoverageReport)

  register<JavaExec>("generateDocumentation") {
    description = "Generates Event documentation"
    classpath = sourceSets["main"].output + configurations["testRuntimeClasspath"] + sourceSets["test"].output
    mainClass.set("uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.DocumentationGenerator")
  }

  register<Test>("unitTest") {
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["main"].output + configurations["testRuntimeClasspath"] + sourceSets["test"].output
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
      jvmTarget = JvmTarget.JVM_25
      freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
  }

  withType<Test> {
    systemProperty("kotest.framework.config.fqn", "uk.gov.justice.digital.hmpps.hmppsintegrationapi.kotest.ProjectConfig")
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

// detekt must use a specific kotlin version when running, this block ensures it's using the correct version
// this is variation on https://detekt.dev/docs/gettingstarted/gradle/#gradle-runtime-dependencies
configurations.matching { it.name == "detekt" }.all {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.jetbrains.kotlin") {
      useVersion(dev.detekt.gradle.plugin.getSupportedKotlinVersion())
    }
  }
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

springBoot {
  mainClass.set("uk.gov.justice.digital.hmpps.hmppsintegrationapi.HmppsIntegrationApiKt")
}
