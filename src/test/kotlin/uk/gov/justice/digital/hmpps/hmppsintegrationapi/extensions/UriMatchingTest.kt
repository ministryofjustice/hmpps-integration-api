package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class UriMatchingTest {
  fun assertAllEqual(vararg strings: String) {
    strings.forEach { s1 ->
      strings.forEach { s2 ->
        s2 shouldBe s1
      }
    }
  }

  @Test
  fun `test uri matching`() {
    assertAllEqual(
      "/v1/persons/${DEFAULT_PATH_PLACEHOLDER}",
      normalisePath("/v1/persons/{hmppsId}"),
      normalisePath("/v1/persons/.*"),
    )
  }
}
