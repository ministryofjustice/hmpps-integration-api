package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway

class CacheKeyTest {
  @Test
  fun `Does not include a null RequestContext in the key generator`() {
    val generator = GatewayKeyGenerator()
    val method = NDeliusGateway::class.java.methods.firstOrNull { it.name == "getOffender" }!!
    val key =
      generator.generate(
        NDeliusGateway("", FeatureFlagConfig()),
        method,
        "A1234AA",
        null,
      )
    assertThat(key).isEqualTo("uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway_getOffender_A1234AA")
  }

  @Test
  fun `Does not include the RequestContext in the key generator`() {
    val generator = GatewayKeyGenerator()
    val method = NDeliusGateway::class.java.methods.firstOrNull { it.name == "getOffender" }!!
    val key =
      generator.generate(
        NDeliusGateway("", FeatureFlagConfig()),
        method,
        "A1234AA",
        buildRequestContext(),
      )
    assertThat(key).isEqualTo("uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway_getOffender_A1234AA")
  }

  @Test
  fun `Includes the oboUserName from the RequestContext in the key generator`() {
    val generator = GatewayKeyGenerator()
    val method = NDeliusGateway::class.java.methods.firstOrNull { it.name == "getOffender" }!!
    val key =
      generator.generate(
        NDeliusGateway("", FeatureFlagConfig()),
        method,
        "A1234AA",
        buildRequestContext(oboUserName = "oboUserName"),
      )
    assertThat(key).isEqualTo("uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway_getOffender_A1234AA_oboUserName")
  }
}
