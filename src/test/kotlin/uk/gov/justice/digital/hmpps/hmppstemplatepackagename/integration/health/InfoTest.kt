package uk.gov.justice.digital.hmpps.hmppstemplatepackagename.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppstemplatepackagename.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InfoTest : IntegrationTestBase() {

  @Test
  fun `Info page is accessible`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("build.name").isEqualTo("hmpps-template-kotlin")
  }

  @Test
  fun `Info page reports version`() {
    webTestClient.get().uri("/info")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("build.version").value<String> {
        assertThat(it).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
      }
  }
}
