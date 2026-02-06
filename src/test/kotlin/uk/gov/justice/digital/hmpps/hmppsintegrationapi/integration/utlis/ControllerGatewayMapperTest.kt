package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.utlis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.UpstreamGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.ControllerGatewayMapper

internal class ControllerGatewayMapperTest(
  @Autowired val context: ApplicationContext,
) : IntegrationTestBase() {
  val mappings = ControllerGatewayMapper().getControllerGatewayMapping(context)

  @Test
  fun `upstream mappings exist`() {
    printAllMappings(mappings)
    assertTrue(mappings.isNotEmpty())
  }

  @Test
  fun `status endpoint uses no upstreams`() {
    assertEquals(0, mappings["controllers.v1.StatusController"]?.size)
  }

  @Test
  fun `config endpoint uses no upstreams`() {
    assertEquals(0, mappings["controllers.v2.ConfigController"]?.size)
  }

  @Test
  fun `prison endpoint uses multiple upstreams`() {
    mappings["controllers.v1.prison.PrisonController"]?.size?.let { assertTrue(it >= 3) }
  }

  @Test
  fun `activities controller uses activities gateway`() {
    assertTrue(mappings["controllers.v1.ActivitiesController"]?.contains("gateways.ActivitiesGateway") == true)
  }

  private fun printAllMappings(mappings: Map<String, Set<String>>) {
    mappings.forEach { (controller, gateways) ->
      println("  $controller -> $gateways")
    }
  }

  @Test
  fun `Upstream gateway metadata is present`() {
    val summaries =
      ControllerGatewayMapper()
        .extApiUpstreamGateways(context)
        .map { it.metaData().summary }
        .toSet()

    assertTrue(summaries.contains("Probation Integration API for NDelius access"))

    val gatewayComponentCount =
      context
        .getBeansWithAnnotation(Component::class.java)
        .values
        .filter {
          it.javaClass.interfaces.contains(UpstreamGateway::class.java) ||
            it.javaClass.superclass.interfaces
              .contains(UpstreamGateway::class.java)
        }.map { it as UpstreamGateway }
        .toList()
        .size

    assertEquals(gatewayComponentCount, summaries.size)
  }
}
