package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exceptions

import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MovedPermanentlyException
import kotlin.test.Test

class MovedPermanentlyExceptionTest {
  @Test
  fun `MovedPermanentlyException swaps upstream location to downstream location with hmppsId`() {
    val ex =
      MovedPermanentlyException(
        "A message",
        "/an-upstream/something/A123456/something",
      )
    ex.toLocation("/downstream/{hmppsId}/something").shouldBe("/downstream/A123456/something")
  }

  @Test
  fun `MovedPermanentlyException swaps upstream location to downstream location without hmppsId`() {
    val ex =
      MovedPermanentlyException(
        "A message",
        "/an-upstream/something/something",
      )
    ex.toLocation("/downstream/something").shouldBe("/downstream/something")
  }

  @Test
  fun `MovedPermanentlyException swaps upstream location to downstream location without location`() {
    val ex =
      MovedPermanentlyException(
        "A message",
        null,
      )
    ex.toLocation("/downstream/something").shouldBe("/downstream/something")
  }

  @Test
  fun `MovedPermanentlyException swaps upstream location to downstream location with empty location`() {
    val ex =
      MovedPermanentlyException(
        "A message",
        "",
      )
    ex.toLocation("/downstream/something").shouldBe("/downstream/something")
  }
}
