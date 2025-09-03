package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ActiveProfiles("test")
class ConsumerFilterConverterTest {
  @Test
  fun `converts empty string to empty RoleFilter object`() {
    val consumerConfig = ""
    val actual = RoleFilterConverter().convert(consumerConfig)

    actual.shouldBe(ConsumerFilters(prisons = null))
  }
}
