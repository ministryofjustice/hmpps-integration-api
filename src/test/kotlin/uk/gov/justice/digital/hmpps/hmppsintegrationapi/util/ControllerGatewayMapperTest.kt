package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

@SpringBootTest
internal class ControllerGatewayMapperTest(
  @Autowired val context: ApplicationContext,
) {
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
}
