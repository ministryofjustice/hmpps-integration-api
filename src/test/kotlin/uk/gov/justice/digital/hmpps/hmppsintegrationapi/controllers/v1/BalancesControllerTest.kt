package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AccountBalance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetBalancesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [BalancesController::class])
@ActiveProfiles("test")
class BalancesControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val getBalancesForPersonService: GetBalancesForPersonService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec({
    val hmppsId = "200313116M"
    val prisonId = "ABC"

    val basePath = "/v1/prison/$prisonId/prisoners/$hmppsId/balances"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)
    val objectMapper = ObjectMapper()
    val balance =
      Balances(
        balances =
          listOf(
            AccountBalance(accountCode = "spends", amount = 101),
            AccountBalance(accountCode = "savings", amount = 102),
            AccountBalance(accountCode = "cash", amount = 103),
          ),
      )

    it("gets the balances for a person with the matching ID") {
      mockMvc.performAuthorised(basePath)

      verify(getBalancesForPersonService, VerificationModeFactory.times(1)).execute(prisonId, hmppsId, null)
    }

    it("returns the correct balances data") {
      whenever(getBalancesForPersonService.execute(prisonId, hmppsId, null)).thenReturn(
        Response(
          data = balance,
        ),
      )
      val result = mockMvc.performAuthorised(basePath)
      result.response.contentAsString.shouldContain(
        """
          "data": {
            "balances": [
              {
                "accountCode": "spends",
                "amount": 101
              },
              {
                "accountCode": "savings",
                "amount": 102
              },
              {
                "accountCode": "cash",
                "amount": 103
              }
            ]
          }
        """.removeWhitespaceAndNewlines(),
      )
    }

    it("calls the API with the correct filters") {
      mockMvc.performAuthorisedWithCN(basePath, "limited-prisons")
      verify(getBalancesForPersonService, times(1)).execute(prisonId, hmppsId, filters = ConsumerFilters(prisons = listOf("XYZ")))
    }

    it("returns a 404 NOT FOUND status code when person isn't found in probation offender search") {
      whenever(getBalancesForPersonService.execute(prisonId, hmppsId, null)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
        ),
      )

      val result = mockMvc.performAuthorised(basePath)

      result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
    }

    it("returns a 404 NOT FOUND status code when person isn't found in Nomis") {
      whenever(getBalancesForPersonService.execute(prisonId, hmppsId, null)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.NOMIS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
        ),
      )

      val result = mockMvc.performAuthorised(basePath)

      result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
    }

    it("returns a 400 BAD REQUEST status code when account isn't found in the upstream API") {
      whenever(getBalancesForPersonService.execute(prisonId, hmppsId, null)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                type = UpstreamApiError.Type.BAD_REQUEST,
                causedBy = UpstreamApi.NOMIS,
              ),
            ),
        ),
      )

      val result = mockMvc.performAuthorised(basePath)

      result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
    }

    it("returns a 500 INTERNAL SERVER ERROR status code when balance isn't found in the upstream API") {
      whenever(getBalancesForPersonService.execute(prisonId, hmppsId, null)).thenThrow(
        IllegalStateException("Error occurred while trying to get accounts for person with id: $hmppsId"),
      )

      val result = mockMvc.performAuthorised(basePath)

      result.response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR.value())
    }
  })
