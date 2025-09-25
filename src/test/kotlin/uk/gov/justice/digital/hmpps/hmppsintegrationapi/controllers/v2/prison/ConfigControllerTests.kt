package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2.prison

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.maps.shouldHaveKeys
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2.ConfigController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ConfigAuthorisation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.RedactionService

@WebMvcTest(controllers = [ConfigController::class])
@ActiveProfiles("test")
class ConfigControllerTests(
  @Autowired var springMockMvc: MockMvc,
  @Autowired var objectMapper: ObjectMapper,
) {
  private val basePath = "/v2/config/authorisation"
  private val mockMvc = IntegrationAPIMockMvc(springMockMvc)

  @MockitoBean
  lateinit var redactionService: RedactionService

  @BeforeEach
  fun beforeTest() {
    doAnswer { invocation ->
      invocation.arguments[0]
    }.whenever(redactionService).applyPolicies(any(), any())
  }

  @Test
  fun `will return authorise config based on application yml`() {
    val result = mockMvc.performAuthorisedWithCN(basePath, "config-v2-test")
    result.response.status.shouldBe(200)

    val actualConfig: Map<String, ConfigAuthorisation> = objectMapper.readValue(result.response.contentAsString)
    actualConfig.shouldHaveKeys("automated-test-client")
    actualConfig["automated-test-client"]!!.filters.shouldBeNull()

    actualConfig.shouldHaveKeys("config-v2-test")
    actualConfig["config-v2-test"].shouldNotBeNull()
    actualConfig["config-v2-test"]!!.endpoints.shouldContainOnly("/v2/config/authorisation")
    actualConfig["config-v2-test"]!!.filters.shouldNotBeNull()
    actualConfig["config-v2-test"]!!.filters!!.prisons.shouldContainOnly("XYZ")
    actualConfig["config-v2-test"]!!.filters!!.caseNotes.shouldBeNull()

    actualConfig.shouldHaveKeys("private-prison-only")
    actualConfig["private-prison-only"].shouldNotBeNull()
    actualConfig["private-prison-only"]!!.endpoints.shouldNotBeEmpty()
    actualConfig["private-prison-only"]!!.filters.shouldBeNull()

    actualConfig.shouldHaveKeys("limited-case-notes")
    actualConfig["limited-case-notes"].shouldNotBeNull()
    actualConfig["limited-case-notes"]!!.endpoints.shouldNotBeEmpty()
    actualConfig["limited-case-notes"]!!.filters.shouldNotBeNull()
    actualConfig["limited-case-notes"]!!.filters!!.prisons.shouldBeNull()
    actualConfig["limited-case-notes"]!!.filters!!.caseNotes.shouldContainOnly("CAB")
  }
}
