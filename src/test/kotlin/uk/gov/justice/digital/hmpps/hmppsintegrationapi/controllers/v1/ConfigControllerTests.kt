package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.maps.shouldHaveKeys
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc

@WebMvcTest(controllers = [ConfigController::class])
@ActiveProfiles("test")
class ConfigControllerTests(
  @Autowired var springMockMvc: MockMvc,
) {
  private val basePath = "/v1/config/authorisation"
  private val mockMvc = IntegrationAPIMockMvc(springMockMvc)
  private val objectMapper = ObjectMapper()

  @Test
  fun `will return authorise config based on application yml`() {
    val result = mockMvc.performAuthorisedWithCN(basePath, "config-test")

    val actualConfig: Map<String, List<String>> = objectMapper.readValue(result.response.contentAsString)
    actualConfig.shouldHaveKeys("automated-test-client")
    actualConfig.shouldHaveKeys("config-test")
    actualConfig["config-test"].shouldContainOnly("/v1/config/authorisation")
  }

  @Test
  fun `will not throw an exception when no includes property exists`() {
    val result = mockMvc.performAuthorisedWithCN(basePath, "no-include")
    result.response.status.shouldBe(403)
  }

  @Test
  fun `will not throw an exception when is an empty includes property exists`() {
    val result = mockMvc.performAuthorisedWithCN(basePath, "empty-include")
    result.response.status.shouldBe(403)
  }
}
