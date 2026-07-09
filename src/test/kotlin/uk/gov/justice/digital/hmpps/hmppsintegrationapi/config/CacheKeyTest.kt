package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.cache.CacheManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway
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

  @Test
  fun `hmppsAuthUsersKeyGenerator generates key from username and authSources`() {
    val cacheManager: CacheManager = mock(CacheManager::class.java)

    val generator = HmppsAuthUsersKeyGenerator()
    val method = ManageUsersGateway::class.java.methods.firstOrNull { it.name == "findUser" }!!
    val key =
      generator.generate(
        ManageUsersGateway("", HmppsAuthGateway(FeatureFlagConfig(), cacheManager, "")),
        method,
        "testUser",
        listOf("AUTH_AZUREAD"),
      )
    assertThat(key).isEqualTo("uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway_findUser_testUser_AUTH_AZUREAD")
  }
}
