package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ActiveProfiles("test")
class ConsumerConfigConverterTest {
  @Test
  fun `converts empty string to empty list`() {
    val consumerConfig = ""
    val actual = ConsumerConfigConverter().convert(consumerConfig)

    actual.shouldBe(ConsumerConfig(emptyList(), ConsumerFilters()))
  }
}
