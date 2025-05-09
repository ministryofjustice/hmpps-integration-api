package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2.prison

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.maps.shouldHaveKeys
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.ConfigController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc

@WebMvcTest(controllers = [ConfigController::class])
@ActiveProfiles("test")
class ConfigControllerTests(
  @Autowired var springMockMvc: MockMvc,
  @Autowired var objectMapper: ObjectMapper,
) {
  private val basePath = "/v2/config/authorisation"
  private val mockMvc = IntegrationAPIMockMvc(springMockMvc)

  @Test
  fun `will return authorise config based on application yml`() {
    val result = mockMvc.performAuthorisedWithCN(basePath, "config-v2-test")

    val actualConfig: Map<String, ConfigControllerConsumerConfig> = objectMapper.readValue(result.response.contentAsString)
    actualConfig.shouldHaveKeys("automated-test-client")
    actualConfig.shouldHaveKeys("config-v2-test")
//    actualConfig["config-v2-test"].get("endpoints:").shouldContainOnly("/v2/config/authorisation")
//    actualConfig.get("private-prison-only").shouldNotBeEmpty()
  }

//  @Test
//  fun `will not throw an exception when no include or roles properties exist`() {
//    val result = mockMvc.performAuthorisedWithCN(basePath, "no-include-or-roles")
//    result.response.status.shouldBe(403)
//  }
//
//  @Test
//  fun `will not throw an exception when an empty includes property exists`() {
//    val result = mockMvc.performAuthorisedWithCN(basePath, "empty-include")
//    result.response.status.shouldBe(403)
//  }
//
//  @Test
//  fun `will not throw an exception when an empty roles property exists`() {
//    val result = mockMvc.performAuthorisedWithCN(basePath, "empty-roles")
//    result.response.status.shouldBe(403)
//  }
}
