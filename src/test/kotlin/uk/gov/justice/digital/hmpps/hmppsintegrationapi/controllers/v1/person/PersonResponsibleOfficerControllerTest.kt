package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonResponsibleOfficerName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Prison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCommunityOffenderManagerForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonOffenderManagerForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@WebMvcTest(controllers = [PersonResponsibleOfficerController::class])
@ActiveProfiles("test")
internal class PersonResponsibleOfficerControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val auditService: AuditService,
  @MockBean val getCommunityOffenderManagerForPersonService: GetCommunityOffenderManagerForPersonService,
  @MockBean val getPrisonOffenderManagerForPersonService: GetPrisonOffenderManagerForPersonService,
) : DescribeSpec() {
  init {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/person-responsible-officer"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getPrisonOffenderManagerForPersonService)
        Mockito.reset(getCommunityOffenderManagerForPersonService)
        whenever(getPrisonOffenderManagerForPersonService.execute(hmppsId)).thenReturn(
          Response(
            PrisonOffenderManager(
              forename = "Paul",
              surname = "Reds",
              prison = Prison(code = "PrisonCode1"),
            ),
          ),
        )

        whenever(getCommunityOffenderManagerForPersonService.execute(hmppsId)).thenReturn(
          Response(
            CommunityOffenderManager(
              name = PersonResponsibleOfficerName("Helen", surname = "Miller"),
              email = "helenemail@email.com",
              telephoneNumber = "0987654321",
              team =
                PersonResponsibleOfficerTeam(
                  code = "PrisonCode2",
                  description = "Description",
                  email = "email_again@email.com",
                  telephoneNumber = "01234567890",
                ),
            ),
          ),
        )
        Mockito.reset(auditService)
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }
    }
  }
}
