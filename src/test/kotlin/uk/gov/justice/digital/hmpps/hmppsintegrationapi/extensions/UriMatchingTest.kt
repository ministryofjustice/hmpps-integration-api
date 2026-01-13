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
  fun `all path parameter placeholders normalise to the same value`() {
    assertAllEqual(
      "/v1/persons/${DEFAULT_PATH_PLACEHOLDER}",
      normalisePath("/v1/persons/${DEFAULT_PATH_PLACEHOLDER}"),
      normalisePath("/v1/persons/{hmppsId}"),
      normalisePath("/v1/persons/{id}"),
      normalisePath("/v1/persons/.*"),
      normalisePath("/v1/persons/.*$"),
      normalisePath("v1/persons/.*$"),
      normalisePath("/v1/persons/[^/]*$"),
    )
  }

  @Test
  fun `multi parameter paths normalise to the same value`() {
    assertAllEqual(
      "/v1/persons/${DEFAULT_PATH_PLACEHOLDER}/images/${DEFAULT_PATH_PLACEHOLDER}",
      normalisePath("/v1/persons/.*/images/.*"),
      normalisePath("/v1/persons/{hmppsId}/images/{imageId}"),
      normalisePath("/v1/persons/.*/images/{imageId}"),
      normalisePath("/v1/persons/{hmppsId}/images/.*"),
      normalisePath("^/v1/persons/.*/images/.*$"),
      normalisePath("v1/persons/{hmppsId}/images/{imageId}"),
    )
  }

  @Test
  fun `full wildcard path is not modified during normalisation`() {
    normalisePath("/.*").shouldBe("/.*")
  }
}
