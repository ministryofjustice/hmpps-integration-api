package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

class ParamsGatewayKeyGeneratorTest {
  @Test
  fun `Does not include the RequestContext in the key generator`() {
    val generator = ParamsGatewayKeyGenerator()
    val method = CorePersonRecordGateway::class.java.methods.firstOrNull { it.name == "corePersonRecordFor" }!!
    val key =
      generator.generate(
        CorePersonRecordGateway(""),
        method,
        GetPersonService.IdentifierType.NOMS,
        "A1234AA",
      )
    assertThat(key).isEqualTo("uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway_corePersonRecordFor_NOMS_A1234AA")
  }
}
