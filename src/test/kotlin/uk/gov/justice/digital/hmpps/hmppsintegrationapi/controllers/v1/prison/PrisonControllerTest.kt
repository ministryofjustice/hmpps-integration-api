package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.prison

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.ENTITY_NOT_FOUND
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDate

@WebMvcTest(controllers = [PrisonController::class])
@ActiveProfiles("test")
internal class PrisonControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec({
    val hmppsId = "200313116M"
    val basePath = "/v1/prison"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $basePath") {
    }

    afterTest {
      Mockito.reset(getPersonService)
      Mockito.reset(auditService)
    }

    it("returns 500 when service throws an exception") {
      whenever(getPersonService.getPrisoner(hmppsId)).thenThrow(RuntimeException("Service error"))

      val result = mockMvc.performAuthorised("$basePath/prisoners/$hmppsId")

      result.response.status.shouldBe(500)
    }

    it("returns a person with all fields populated") {
      whenever(getPersonService.getPrisoner(hmppsId)).thenReturn(
        Response(
          data =
            Person(
              firstName = "Barry",
              lastName = "Allen",
              middleName = "Jonas",
              dateOfBirth = LocalDate.parse("2023-03-01"),
              gender = "Male",
              ethnicity = "Caucasian",
              pncId = "PNC123456",
              currentExclusion = true,
              exclusionMessage = "An exclusion is present",
              currentRestriction = true,
              restrictionMessage = "A restriction is present",
            ),
        ),
      )

      val result = mockMvc.performAuthorised("$basePath/prisoners/$hmppsId")

      result.response.contentAsString.shouldBe(
        """
            {
             "data":{
                   "firstName":"Barry",
                   "lastName":"Allen",
                   "middleName":"Jonas",
                   "dateOfBirth":"2023-03-01",
                   "gender":"Male",
                   "ethnicity":"Caucasian",
                   "aliases":[],
                   "identifiers":{
                      "nomisNumber":null,
                      "croNumber":null,
                      "deliusCrn":null
                   },
                   "pncId": "PNC123456",
                   "hmppsId": null,
                   "contactDetails": null,
                   "currentRestriction": true,
                   "restrictionMessage": "A restriction is present",
                   "currentExclusion": true,
                   "exclusionMessage": "An exclusion is present"
                }
             }
          """.removeWhitespaceAndNewlines(),
      )
    }

    it("logs audit event") {
      whenever(getPersonService.getPrisoner(hmppsId)).thenReturn(
        Response(
          data =
            Person(
              firstName = "Barry",
              lastName = "Allen",
              middleName = "Jonas",
              dateOfBirth = LocalDate.parse("2023-03-01"),
              gender = "Male",
              ethnicity = "Caucasian",
              pncId = "PNC123456",
            ),
        ),
      )

      mockMvc.performAuthorised("$basePath/prisoners/$hmppsId")
      verify(
        auditService,
        times(1),
      ).createEvent(
        "GET_PERSON_DETAILS",
        mapOf("hmppsId" to hmppsId),
      )
    }

    it("returns 404 when prisoner is not found") {
      whenever(getPersonService.getPrisoner(hmppsId)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                type = ENTITY_NOT_FOUND,
                causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                description = "Prisoner not found",
              ),
            ),
        ),
      )

      val result = mockMvc.performAuthorised("$basePath/prisoners/$hmppsId")

      result.response.status.shouldBe(404)
    }
  })
