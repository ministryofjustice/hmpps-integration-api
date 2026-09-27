package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.documentation

import kotlin.test.Test
import kotlin.test.assertEquals

class EndpointToGatewayDocumentationManagerTest {
  @Test
  fun `Should parse different mapping styles`() {
    val x =
      listOf(
        """@RequestMapping("/v1/persons")""",
        """@RequestMapping(value = ["/v1/prison"])""",
        """@GetMapping""",
        """@GetMapping("/v1/prison")""",
        """@RequestMapping(method = [RequestMethod.GET, RequestMethod.POST], value = ["addresses"])""",
        """@RequestMapping(value = ["addresses"], method = [RequestMethod.GET, RequestMethod.POST])""",
        """@PostMapping("/v1/prison")""",
        """@PutMapping("/v1/prison")""",
      )

    val expected =
      listOf(
        Pair("/v1/persons", "GET"),
        Pair("/v1/prison", "GET"),
        Pair("", "GET"),
        Pair("/v1/prison", "GET"),
        Pair("addresses", "GET,POST"),
        Pair("addresses", "GET,POST"),
        Pair("/v1/prison", "POST"),
        Pair("/v1/prison", "PUT"),
      )

    val result =
      x.map {
        extractFromAnnotation(it)
      }

    assertEquals(expected, result)
  }

  @Test
  fun `Should generate documentation data`() {
    val generator = EndpointToGatewayDocumentationManager()
    val data = generator.getData()
    val endPoint = data["GET /v1/persons/{hmppsId}/risks/scores"]
    val gateway = endPoint?.firstOrNull { it.inClass.lowercase().contains("gateway") }

    assertEquals("Assess Risks and Needs", gateway?.metadata?.summary)
    assertEquals(98, data.size)
  }
}
